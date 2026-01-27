package com.example.straffic.parking.controller;

import com.example.straffic.parking.entity.ParkingRecordEntity;
import com.example.straffic.parking.entity.ParkingSpotEntity;
import com.example.straffic.parking.repository.ParkingRecordRepository;
import com.example.straffic.parking.repository.ParkingSpotRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingApiController {

    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingRecordRepository parkingRecordRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        // 기존 50개 등 불필요한 구역 데이터 정리 (A-1 ~ A-10 이외의 데이터 삭제)
        List<ParkingSpotEntity> allSpots = parkingSpotRepository.findAll();
        for (ParkingSpotEntity spot : allSpots) {
            String id = spot.getSpotId();
            // "A-1" ~ "A-10" 형식이 아니거나, 범위를 벗어나는 경우 삭제
            // 간단하게: 1~10 범위에 포함되는지 확인
            boolean isValid = false;
            if (id.startsWith("A-")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num >= 1 && num <= 10) {
                        isValid = true;
                    }
                } catch (NumberFormatException ignored) {}
            }
            
            if (!isValid) {
                parkingSpotRepository.delete(spot);
            }
        }

        // A구역 10개 주차 공간 생성 (A-1 ~ A-10)
        for (int i = 1; i <= 10; i++) {
            String spotId = "A-" + i;
            if (!parkingSpotRepository.existsById(spotId)) {
                parkingSpotRepository.save(new ParkingSpotEntity(spotId));
            }
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        List<ParkingSpotEntity> spots = parkingSpotRepository.findAll(Sort.by("spotId"));
        
        Map<String, Object> spotsMap = new HashMap<>();
        for (ParkingSpotEntity spot : spots) {
            spotsMap.put(spot.getSpotId(), spot);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("spots", spotsMap);
        
        // 통계 정보
        long occupied = spots.stream().filter(ParkingSpotEntity::isOccupied).count();
        long total = spots.size();
        response.put("statistics", Map.of(
            "total", total,
            "occupied", occupied,
            "available", total - occupied
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/entry")
    public ResponseEntity<Map<String, Object>> entry(@RequestBody Map<String, String> request) {
        String plateNumber = request.get("plateNumber");
        String spotId = request.get("spotId");

        System.out.println("[DEBUG] Entry Request - Spot: [" + spotId + "], Plate: [" + plateNumber + "]");

        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "차량 번호를 입력하세요"));
        }
        if (spotId == null || spotId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "주차 구역을 선택하세요"));
        }

        Optional<ParkingSpotEntity> spotOpt = parkingSpotRepository.findById(spotId);
        ParkingSpotEntity spot;

        if (spotOpt.isEmpty()) {
            // 구역이 DB에 없는 경우, 유효한 구역(A-1 ~ A-10)이면 즉시 생성하여 복구
            if (isValidSpotId(spotId)) {
                System.out.println("[INFO] 구역 [" + spotId + "]이 DB에 없어 새로 생성합니다.");
                spot = new ParkingSpotEntity(spotId);
                parkingSpotRepository.save(spot);
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "존재하지 않는 구역입니다 (" + spotId + ")"));
            }
        } else {
            spot = spotOpt.get();
        }

        if (spot.isOccupied()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "이미 주차된 구역입니다 (" + spot.getPlateNumber() + ")"));
        }

        try {
            // 입차 처리
            spot.setOccupied(true);
            spot.setPlateNumber(plateNumber.trim());
            spot.setEntryTime(LocalDateTime.now());
            parkingSpotRepository.save(spot);
            
            // 확실한 저장을 위해 flush
            parkingSpotRepository.flush();
            System.out.println("[SUCCESS] 입차 완료 - " + spotId + " : " + plateNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "DB 저장 중 오류 발생: " + e.getMessage()));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", spotId + " 구역에 입차 완료");
        response.put("spot", spot);
        response.put("entryTime", spot.getEntryTime().format(formatter));

        return ResponseEntity.ok(response);
    }

    private boolean isValidSpotId(String spotId) {
        if (spotId == null || !spotId.startsWith("A-")) return false;
        try {
            int num = Integer.parseInt(spotId.substring(2));
            return num >= 1 && num <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @PostMapping("/exit")
    public ResponseEntity<Map<String, Object>> exit(@RequestBody Map<String, String> request) {
        String spotId = request.get("spotId");

        if (spotId == null || spotId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "주차 구역을 선택하세요"));
        }

        Optional<ParkingSpotEntity> spotOpt = parkingSpotRepository.findById(spotId);
        if (spotOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "존재하지 않는 구역입니다"));
        }

        ParkingSpotEntity spot = spotOpt.get();
        if (!spot.isOccupied()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "주차된 차량이 없습니다"));
        }

        // 주차 요금 계산 (분당 50원)
        LocalDateTime entryTime = spot.getEntryTime();
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(entryTime, now).toMinutes();
        if (minutes < 1) minutes = 1; 
        int fee = (int) (minutes * 50);

        // 기록 저장
        ParkingRecordEntity record = new ParkingRecordEntity();
        record.setParkingSpot(spotId);
        record.setCarNumber(spot.getPlateNumber());
        record.setCarType("일반"); // 기본값
        record.setEntryTime(entryTime);
        record.setExitTime(now);
        record.setDurationMinutes(minutes);
        record.setFee(fee);
        parkingRecordRepository.save(record);

        // 출차 처리 (Spot 초기화)
        String plateNumber = spot.getPlateNumber();
        spot.setOccupied(false);
        spot.setPlateNumber(null);
        spot.setEntryTime(null);
        parkingSpotRepository.save(spot);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "출차 완료");
        response.put("plateNumber", plateNumber);
        response.put("duration", minutes + "분");
        response.put("fee", fee);
        response.put("exitTime", now.format(formatter));

        return ResponseEntity.ok(response);
    }
}
