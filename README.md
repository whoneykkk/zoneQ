# ZoneQ — 독서실 소음 관리 시스템

브라우저 마이크로 소음을 측정하고, 등급을 산정해 구역별 좌석을 배치하는 독서실 관리 플랫폼입니다.

## 주요 기능

- **실시간 소음 측정** — Web Audio API 기반 dB 측정 및 서버 전송
- **소음 등급 산정** — S / A / B / C 4단계 등급 (Leq 평균·피크 빈도·개선 추이 반영)
- **구역별 좌석 배치** — 등급에 따라 S·A·B·C 구역 자동 배정
- **관리자 대시보드** — 좌석별 실시간 dB 모니터링 (SSE)
- **쪽지 시스템** — 이용자 간 익명·실명 쪽지 발송
- **공지사항** — 관리자 공지 등록·수정·삭제

## 소음 등급 기준

| 등급 | 기준 |
|------|------|
| S | 40 dB 이하 |
| A | 40 ~ 50 dB |
| B | 50 ~ 60 dB |
| C | 60 dB 초과 |

## 기술 스택

### Backend
- Java 21 / Spring Boot 4.x
- Spring Security + JWT (Access / Refresh Token)
- Spring Data JPA + QueryDSL
- MySQL (개발: H2)
- Gradle

### Frontend
- React + Vite
- Tailwind CSS

## 프로젝트 구조

```
ZoneQ/
├── backend/          # Spring Boot API 서버
│   └── src/main/java/com/zoneq/
│       ├── domain/   # auth / user / seat / noise / grade / message / notice / notification
│       └── global/   # config / exception / response / security
└── frontend/         # React 클라이언트
```

## 실행 방법

### Backend

```bash
cd backend
./gradlew bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## API 개요

| 도메인 | 경로 프리픽스 |
|--------|--------------|
| 인증 | `/api/auth` |
| 소음 | `/api/noise` |
| 등급 | `/api/grades` |
| 좌석 | `/api/seats` |
| 쪽지 | `/api/messages` |
| 공지 | `/api/notices` |
| 알림 | `/api/notifications` |
| 대시보드 | `/api/dashboard` |
| 프로필 | `/api/profile` |

인증이 필요한 엔드포인트는 `Authorization: Bearer {accessToken}` 헤더를 사용합니다.
