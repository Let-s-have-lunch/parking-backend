# 🚗 Parking Map & Realtime Service (Backend)

전국 공영주차장 표준 데이터와 서울시 실시간 주차장 현황 Open API를 연동 및 동기화하고, Spring WebFlux와 Spring Security(JWT)를 기반으로 고성능 인증 및 주차장 검색/즐겨찾기 기능을 제공하는 백엔드 서버입니다.

---

## 🛠 Tech Stack

*   **Core:** Spring Boot, Spring WebFlux (Reactor), Java
*   **Database:** MariaDB, Spring Data JPA
*   **Security:** Spring Security, JJWT (v0.13.0) - JWT 기반 인증
*   **Validation:** Jakarta Validation
*   **Build Tool:** Gradle
*   **External API:** 공공데이터포털(전국공영주차장정보표준), 공공데이터포털(서울시 시영주차장 실시간 주차정보)

---

## ✨ Key Features

1.  **위치 및 영역 기반 주차장 검색 (WebFlux)**
    *   지도 뷰포트 영역(Bounding Box, 위경도 범위) 내에 포함되는 주차장 목록 고속 조회
    *   논블로킹 리액티브 스트림(`Flux`, `Mono`) 구조를 통한 대용량 좌표 연산 최적화
2.  **실시간 주차 현황 병합 (Realtime Sync)**
    *   주차장 단건 상세 조회 시 서울시 실시간 주차 현황 API와 연동하여 잔여 주차면수 및 혼잡도 실시간 계산 및 병합
3.  **사용자 인증 및 보안 (Spring Security + JWT)**
    *   비동기/리액티브 환경에 최적화된 WebFlux 기반 Spring Security FilterChain 구현
    *   AccessToken 기반 회원가입, 로그인 및 인가 처리
4.  **사용자 즐겨찾기 (Favorites)**
    *   로그인한 유저가 선호하는 주차장을 등록/해제(토글)할 수 있는 즐겨찾기 시스템
    *   지연 로딩(Lazy) 이슈 방지를 위한 `JOIN FETCH` 최적화 적용

---

## 🔌 API Endpoints Specification

| 분류 | Method | Endpoint | Description | Auth 여부 |
| :--- | :--- | :--- | :--- | :--- |
| **인증 (Auth)** | `POST` | `/auth/login` | 사용자 로그인 및 JWT 발급 | 🔓 Public |
| | `POST` | `/auth/signup` | 회원가입 | 🔓 Public |
| **주차장 (Parking)**| `GET` | `/parking-lots` | 지도 영역(Bounds) 내 주차장 목록 조회 | 🔓 Public |
| | `GET` | `/parking-lots/{id}` | 주차장 단건 상세 조회 (실시간 데이터 및 즐겨찾기 여부 병합) | 🔓 Optional |
| **즐겨찾기 (Favorite)**| `POST` | `/favorites/{id}` | 특정 주차장 즐겨찾기 추가/삭제 (토글) | 🔒 Private |
| | `GET` | `/favorites` | 로그인한 유저의 즐겨찾기 주차장 목록 조회 | 🔒 Private |

---

## 📂 Project Structure

```text
com.lineacademy.parkingbackendprev/
├── config/                  # SecurityConfig, Jwt 설정 등
├── controller/              # ParkingLotController, FavoriteParkingLotController 등
├── domain/                  
│   ├── entity/              # User, ParkingLot, FavoriteParkingLot 등
│   ├── common/              # BaseTimeEntity
│   └── enums/               # UserRole 등
├── dto/                     # Request / Response DTO 객체
├── repository/              # JpaRepository 인터페이스 그룹
└── service/                 # 비즈니스 로직 및 외부 API 연동 서비스
```

---

## ⚙️ Configuration (`application.yml`)

서버 실행을 위한 주요 설정 정보입니다. (DB 및 외부 OpenAPI Key 설정 포함)

```yaml
server:
  port: 8007

spring:
  datasource:
    url: jdbc:mariadb://${DB_HOSTNAME}:${DB_PORT}/${DB_SCHEMA}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

jwt:
  secret: ${JWT_SECRET_KEY}
  expiration: 86400000 # 24시간 (밀리초 단위)

public:
  api:
    key: ${PUBLIC_PARKING_API_KEY}
    url: https://api.data.go.kr/openapi/tn_pubr_prkplce_info_api
realtime:
  api:
    key: ${SEOUL_REALTIME_API_KEY}
    url: https://api.odcloud.kr/api
```
---

## 🚀 Getting Started
### 1. Prerequisite
- Java 17 이상
- MariaDB
- Gradle

### 2. Build & Run
프로젝트 루트 디렉토리에서 빌드 및 실행을 진행합니다.
```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```