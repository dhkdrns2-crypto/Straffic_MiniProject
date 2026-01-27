# 시작하기 (Getting Started)

### 참고 문서 (Reference Documentation)
더 자세한 내용은 다음 섹션을 참조하세요:

* [공식 Gradle 문서](https://docs.gradle.org)
* [Spring Boot Gradle 플러그인 참조 가이드](https://docs.spring.io/spring-boot/3.5.0/gradle-plugin)
* [OCI 이미지 생성](https://docs.spring.io/spring-boot/3.5.0/gradle-plugin/packaging-oci-image.html)
* [GraalVM 네이티브 이미지 지원](https://docs.spring.io/spring-boot/3.5.0/reference/packaging/native-image/introducing-graalvm-native-images.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.0/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.0/reference/using/devtools.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.0/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation (유효성 검사)](https://docs.spring.io/spring-boot/3.5.0/reference/io/validation.html)
* [Spring Security](https://docs.spring.io/spring-boot/3.5.0/reference/web/spring-security.html)

### 가이드 (Guides)
다음 가이드들은 일부 기능의 구체적인 사용법을 설명합니다:

* [RESTful 웹 서비스 구축](https://spring.io/guides/gs/rest-service/)
* [Spring MVC로 웹 콘텐츠 서비스하기](https://spring.io/guides/gs/serving-web-content/)
* [Spring으로 REST 서비스 구축](https://spring.io/guides/tutorials/rest/)
* [JPA로 데이터 액세스하기](https://spring.io/guides/gs/accessing-data-jpa/)
* [폼 입력 유효성 검사](https://spring.io/guides/gs/validating-form-input/)
* [웹 애플리케이션 보안 설정](https://spring.io/guides/gs/securing-web/)
* [Spring Boot와 OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [LDAP로 사용자 인증하기](https://spring.io/guides/gs/authenticating-ldap/)

### 추가 링크 (Additional Links)
다음 추가 링크들도 도움이 될 수 있습니다:

* [Gradle Build Scans – 프로젝트 빌드에 대한 인사이트](https://scans.gradle.com#gradle)
* [빌드 플러그인에서 AOT 설정 구성](https://docs.spring.io/spring-boot/3.5.0/how-to/aot.html)

## GraalVM 네이티브 지원 (GraalVM Native Support)

이 프로젝트는 경량 컨테이너 또는 네이티브 실행 파일을 생성할 수 있도록 구성되어 있습니다.
네이티브 이미지에서 테스트를 실행하는 것도 가능합니다.

### Cloud Native Buildpacks를 사용한 경량 컨테이너
Spring Boot 컨테이너 이미지 지원에 이미 익숙하다면 이 방법이 가장 쉽게 시작할 수 있는 방법입니다.
이미지를 생성하기 전에 Docker가 설치되어 있고 구성되어 있어야 합니다.

이미지를 생성하려면 다음 명령을 실행하세요:

```
$ ./gradlew bootBuildImage
```

그런 다음 다른 컨테이너처럼 앱을 실행할 수 있습니다:

```
$ docker run --rm -p 8080:8080 coffee:0.0.1-SNAPSHOT
```

### Native Build Tools를 사용한 실행 파일
테스트를 네이티브 이미지에서 실행하는 등 더 많은 옵션을 탐색하려면 이 옵션을 사용하세요.
GraalVM `native-image` 컴파일러가 설치되어 있고 구성되어 있어야 합니다.

참고: GraalVM 22.3 이상이 필요합니다.

실행 파일을 생성하려면 다음 명령을 실행하세요:

```
$ ./gradlew nativeCompile
```

그런 다음 다음과 같이 앱을 실행할 수 있습니다:
```
$ build/native/nativeCompile/coffee
```

기존 테스트 모음을 네이티브 이미지에서 실행할 수도 있습니다.
이는 애플리케이션의 호환성을 검증하는 효율적인 방법입니다.

기존 테스트를 네이티브 이미지에서 실행하려면 다음 명령을 실행하세요:

```
$ ./gradlew nativeTest
```

### Gradle Toolchain 지원

Native Build Tools와 Gradle toolchains와 관련하여 몇 가지 제한 사항이 있습니다.
Native Build Tools는 기본적으로 toolchain 지원을 비활성화합니다.
사실상 네이티브 이미지 컴파일은 Gradle을 실행하는 데 사용된 JDK로 수행됩니다.
[Native Build Tools의 toolchain 지원에 대한 자세한 내용은 여기](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html#configuration-toolchains)에서 확인할 수 있습니다.

---

## 🛠️ 프로젝트 실행 및 라이브러리 설치 (Installation & Execution)

이 프로젝트는 **Java Spring Boot 백엔드**와 **Python Flask OCR 서비스**를 함께 실행해야 정상적으로 작동합니다.

### 1. Python 라이브러리 설치 (OCR 서비스용)
주차 번호판 인식(OCR) 기능을 사용하려면 Python 환경 설정이 필요합니다.
아래 명령어를 통해 필요한 라이브러리를 한 번에 설치할 수 있습니다.

**방법 1: requirements.txt 파일 이용 (권장)**
```bash
pip install -r ocr-service/requirements.txt
```

**방법 2: 개별 설치**
```bash
pip install flask flask-cors Pillow easyocr numpy opencv-python ultralytics
```

### 2. 서버 실행 방법

**① Spring Boot 메인 서버 실행:**
```bash
./gradlew bootRun
```
* 서버 포트: `1111`
* 접속 주소: `http://localhost:1111`

**② OCR 서비스 실행:**
새 터미널을 열고 다음 명령어를 실행하세요:
```bash
python ocr-service/parking_ocr_service.py
```
* OCR 서버 포트: `5000`
