#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
S-MaaS 주차장 OCR 서비스
YOLO8 번호판 탐지 + EasyOCR 텍스트 인식
"""

import os
import re
import io
import sys
import ssl
from datetime import datetime

# SSL 인증서 검증 비활성화 (EasyOCR 모델 다운로드 문제 해결)
try:
    _create_unverified_https_context = ssl._create_unverified_context
except AttributeError:
    pass
else:
    ssl._create_default_https_context = _create_unverified_https_context

from flask import Flask, request, jsonify
from flask_cors import CORS

# Pillow 호환성 패치
try:
    from PIL import Image
    if not hasattr(Image, 'ANTIALIAS'):
        Image.ANTIALIAS = Image.LANCZOS
    print("[INFO] Pillow 패치 완료")
except Exception as e:
    print(f"[WARN] Pillow 패치 실패: {e}")

import numpy as np

# YOLO8 로드
YOLO_AVAILABLE = False
yolo_model = None
try:
    from ultralytics import YOLO
    YOLO_AVAILABLE = True
    print("[INFO] Ultralytics YOLO 로드 성공")
except ImportError as e:
    print(f"[WARN] YOLO 사용 불가: {e}")
    print("[INFO] pip install ultralytics 로 설치하세요")

# EasyOCR 로드
EASYOCR_AVAILABLE = False
ocr_reader = None
try:
    import easyocr
    EASYOCR_AVAILABLE = True
    print("[INFO] EasyOCR 로드 성공")
except ImportError as e:
    print(f"[WARN] EasyOCR 사용 불가: {e}")

# OpenCV 로드
CV2_AVAILABLE = False
try:
    import cv2
    CV2_AVAILABLE = True
    print("[INFO] OpenCV 로드 성공")
except ImportError:
    print("[WARN] OpenCV 사용 불가")

app = Flask(__name__)
CORS(app)

# 설정
UPLOAD_FOLDER = os.path.join(os.path.dirname(__file__), 'uploads')
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'}

# 한국 번호판 패턴
PLATE_PATTERNS = [
    r'(\d{2,3})\s*([가-힣])\s*(\d{4})',           # 일반: 12가1234, 123가1234
    r'([가-힣]{2})\s*(\d{2})\s*([가-힣])\s*(\d{4})',  # 신형: 서울12가1234
    r'(\d{2})\s*([가-힣]{2})\s*(\d{4})',           # 영업용: 12서울1234
]


def init_models():
    """모델 초기화"""
    global yolo_model, ocr_reader

    # YOLO8 모델 로드
    if YOLO_AVAILABLE and yolo_model is None:
        try:
            print("[INFO] YOLO8 모델 로드 중...")
            yolo_model = YOLO('yolov8n.pt')
            print("[INFO] YOLO8 모델 로드 완료!")
        except Exception as e:
            print(f"[ERROR] YOLO8 모델 로드 실패: {e}")

    # EasyOCR Reader 초기화
    if EASYOCR_AVAILABLE and ocr_reader is None:
        try:
            print("[INFO] EasyOCR Reader 초기화 중...")
            ocr_reader = easyocr.Reader(['ko', 'en'], gpu=False)
            print("[INFO] EasyOCR Reader 초기화 완료!")
        except Exception as e:
            print(f"[ERROR] EasyOCR 초기화 실패: {e}")


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


def preprocess_for_ocr(image):
    """OCR을 위한 이미지 전처리"""
    if not CV2_AVAILABLE:
        return np.array(image) if isinstance(image, Image.Image) else image

    try:
        img = np.array(image) if isinstance(image, Image.Image) else image

        if len(img.shape) == 3 and img.shape[2] == 3:
            img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY) if len(img.shape) == 3 else img
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(gray)
        denoised = cv2.bilateralFilter(enhanced, 11, 17, 17)

        return denoised
    except Exception as e:
        print(f"[WARN] 전처리 실패: {e}")
        return np.array(image) if isinstance(image, Image.Image) else image


def detect_plate_yolo(image):
    """YOLO8로 번호판 영역 탐지"""
    if not YOLO_AVAILABLE or yolo_model is None:
        print("[DEBUG] YOLO 사용 불가")
        return None, []

    try:
        img_array = np.array(image) if isinstance(image, Image.Image) else image
        print(f"[DEBUG] YOLO 탐지 시작: shape={img_array.shape}")

        results = yolo_model(img_array, verbose=False)
        detected_regions = []

        for result in results:
            boxes = result.boxes
            print(f"[DEBUG] YOLO 탐지 객체 수: {len(boxes)}")

            for box in boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0].tolist())
                conf = float(box.conf[0])
                cls = int(box.cls[0])
                class_name = yolo_model.names.get(cls, 'unknown')

                w, h = x2 - x1, y2 - y1
                aspect_ratio = w / max(h, 1)

                print(f"[DEBUG] 객체: {class_name}, 신뢰도: {conf:.2f}, 비율: {aspect_ratio:.2f}")

                detected_regions.append({
                    'bbox': (x1, y1, x2, y2),
                    'confidence': conf,
                    'class': class_name,
                    'aspect_ratio': aspect_ratio
                })

        # 번호판 비율에 가까운 영역 필터
        plate_candidates = [r for r in detected_regions if 1.5 < r['aspect_ratio'] < 6.0]

        if plate_candidates:
            best = max(plate_candidates, key=lambda x: x['confidence'])
            x1, y1, x2, y2 = best['bbox']

            margin = 10
            h, w = img_array.shape[:2]
            x1, y1 = max(0, x1 - margin), max(0, y1 - margin)
            x2, y2 = min(w, x2 + margin), min(h, y2 + margin)

            cropped = img_array[y1:y2, x1:x2]
            print(f"[DEBUG] YOLO 크롭: {cropped.shape}")
            return cropped, detected_regions

        return None, detected_regions

    except Exception as e:
        print(f"[ERROR] YOLO 탐지 실패: {e}")
        return None, []


def detect_plate_contour(image):
    """Contour Detection으로 번호판 영역 찾기"""
    if not CV2_AVAILABLE:
        return None

    try:
        img_array = np.array(image) if isinstance(image, Image.Image) else image

        if len(img_array.shape) == 3 and img_array.shape[2] == 3:
            img_bgr = cv2.cvtColor(img_array, cv2.COLOR_RGB2BGR)
        else:
            img_bgr = img_array

        gray = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2GRAY) if len(img_bgr.shape) == 3 else img_bgr
        edges = cv2.Canny(gray, 100, 200)
        contours, _ = cv2.findContours(edges, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
        contours = sorted(contours, key=cv2.contourArea, reverse=True)[:10]

        for contour in contours:
            peri = cv2.arcLength(contour, True)
            approx = cv2.approxPolyDP(contour, 0.02 * peri, True)

            if len(approx) == 4:
                x, y, w, h = cv2.boundingRect(approx)
                ratio = w / float(h)

                if 1.5 < ratio < 6.0 and w > 60 and h > 20:
                    margin = 5
                    plate = img_bgr[max(0, y-margin):y+h+margin, max(0, x-margin):x+w+margin]
                    print(f"[DEBUG] Contour 번호판: {plate.shape}")
                    return plate

        return None
    except Exception as e:
        print(f"[WARN] Contour 실패: {e}")
        return None


def extract_plate_text(texts):
    """OCR 결과에서 번호판 패턴 추출"""
    combined = ' '.join(texts)
    no_space = combined.replace(' ', '').replace('\n', '')

    print(f"[DEBUG] OCR 텍스트: '{combined}'")

    for pattern in PLATE_PATTERNS:
        match = re.search(pattern, combined)
        if match:
            result = ''.join(match.groups())
            print(f"[DEBUG] 패턴 매칭: '{result}'")
            return result

        match = re.search(pattern.replace(r'\s*', ''), no_space)
        if match:
            result = ''.join(match.groups())
            print(f"[DEBUG] 패턴 매칭(무공백): '{result}'")
            return result

    return None


def recognize_with_ocr(image):
    """EasyOCR로 텍스트 인식"""
    if not EASYOCR_AVAILABLE or ocr_reader is None:
        print("[ERROR] EasyOCR 사용 불가")
        return None, []

    try:
        img_array = np.array(image) if isinstance(image, Image.Image) else image
        print(f"[DEBUG] OCR 시작: shape={img_array.shape}")

        results = ocr_reader.readtext(img_array)
        texts = []

        for (bbox, text, conf) in results:
            print(f"[DEBUG] OCR: '{text}' ({conf:.2f})")
            texts.append(text)

        plate_number = extract_plate_text(texts)
        return plate_number, texts

    except Exception as e:
        print(f"[ERROR] OCR 실패: {e}")
        return None, []


@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'healthy',
        'service': 'S-MaaS Parking OCR (YOLO8 + EasyOCR)',
        'yolo_available': YOLO_AVAILABLE,
        'easyocr_available': EASYOCR_AVAILABLE,
        'opencv_available': CV2_AVAILABLE,
        'timestamp': datetime.now().isoformat()
    })


@app.route('/api/parking/recognize', methods=['POST'])
def recognize_plate():
    """번호판 인식 API"""
    print("\n" + "=" * 60)
    print("[INFO] /api/parking/recognize 요청")
    print("=" * 60)

    try:
        if 'image' in request.files:
            file = request.files['image']
        elif 'file' in request.files:
            file = request.files['file']
        else:
            return jsonify({'success': False, 'error': '파일 없음 (image or file required)', 'plateNumber': None}), 400

        print(f"[DEBUG] 파일: {file.filename}")

        if file.filename == '' or not allowed_file(file.filename):
            return jsonify({'success': False, 'error': '잘못된 파일', 'plateNumber': None}), 400

        # 이미지 로드
        file_bytes = file.read()
        image = Image.open(io.BytesIO(file_bytes))

        if image.mode != 'RGB':
            image = image.convert('RGB')

        print(f"[DEBUG] 이미지: size={image.size}")

        plate_number = None
        raw_texts = []
        yolo_count = 0

        # 1단계: YOLO8 번호판 탐지
        plate_region, yolo_detections = detect_plate_yolo(image)
        yolo_count = len(yolo_detections)

        if plate_region is not None:
            print("[DEBUG] YOLO 영역에서 OCR")
            preprocessed = preprocess_for_ocr(plate_region)
            plate_number, raw_texts = recognize_with_ocr(preprocessed)

        # 2단계: Contour Detection
        if plate_number is None:
            print("[DEBUG] Contour Detection")
            contour_region = detect_plate_contour(image)
            if contour_region is not None:
                preprocessed = preprocess_for_ocr(contour_region)
                plate_number, raw_texts = recognize_with_ocr(preprocessed)

        # 3단계: 전처리 전체 이미지
        if plate_number is None:
            print("[DEBUG] 전처리 전체 이미지")
            preprocessed = preprocess_for_ocr(image)
            plate_number, raw_texts = recognize_with_ocr(preprocessed)

        # 4단계: 원본 이미지
        if plate_number is None:
            print("[DEBUG] 원본 이미지")
            plate_number, raw_texts = recognize_with_ocr(image)

        # 결과
        if plate_number:
            print(f"[SUCCESS] 번호판: {plate_number}")
            return jsonify({
                'success': True,
                'plates': [{'plate_number': plate_number}],
                'plateNumber': plate_number,
                'rawTexts': raw_texts,
                'yoloDetections': yolo_count,
                'confidence': 0.95,
                'timestamp': datetime.now().isoformat()
            })
        else:
            raw_text = ' '.join(raw_texts) if raw_texts else ''
            print(f"[WARN] 패턴 없음: '{raw_text}'")
            return jsonify({
                'success': False,
                'plateNumber': raw_text if raw_text else None,
                'rawTexts': raw_texts,
                'yoloDetections': yolo_count,
                'error': '번호판 패턴을 찾을 수 없습니다.',
                'timestamp': datetime.now().isoformat()
            })

    except Exception as e:
        print(f"[ERROR] {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e), 'plateNumber': None}), 500


@app.route('/api/parking/calculate', methods=['POST'])
def calculate_fee():
    """주차 요금 계산"""
    try:
        data = request.get_json()
        entry_time = datetime.fromisoformat(data.get('entryTime'))
        exit_time = datetime.fromisoformat(data.get('exitTime'))
        car_type = data.get('carType', '일반')

        minutes = max(1, int((exit_time - entry_time).total_seconds() / 60))

        if minutes <= 30:
            fee = 1000
        else:
            extra = minutes - 30
            fee = 1000 + ((extra + 9) // 10) * 500

        discount = {'경차': 0.5, '전기차': 0.3, '장애인': 1.0}.get(car_type, 0)
        final_fee = int(fee * (1 - discount))

        return jsonify({
            'success': True,
            'durationMinutes': minutes,
            'baseFee': fee,
            'discount': discount,
            'finalFee': final_fee
        })
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500


if __name__ == '__main__':
    print("\n" + "=" * 60)
    print("  S-MaaS 주차장 OCR 서비스")
    print("  YOLO8 + EasyOCR 번호판 인식")
    print("=" * 60)

    init_models()

    print(f"  YOLO8:   {'OK' if YOLO_AVAILABLE else 'X'}")
    print(f"  EasyOCR: {'OK' if EASYOCR_AVAILABLE else 'X'}")
    print(f"  OpenCV:  {'OK' if CV2_AVAILABLE else 'X'}")
    print("=" * 60)
    print("  http://localhost:5000")
    print("=" * 60 + "\n")

    app.run(host='0.0.0.0', port=5000, debug=True)
