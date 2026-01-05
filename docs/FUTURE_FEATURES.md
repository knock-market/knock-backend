# Future Features Roadmap
MVP 구현 이후 서비스의 완성도를 높이고 사용자 경험을 개선하기 위한 추가 기능 제안서입니다.

## 1. 💬 댓글 및 문의 (Item Comments)
사용자들이 물건에 대해 공개적으로 문의할 수 있는 기능입니다.

### 기능 명세
- **댓글 작성:** 물건 상세 페이지에서 댓글 등록 (비밀글 옵션 포함 가능)
- **대댓글:** 댓글에 대한 답글 작성 (판매자의 답변)
- **알림 연동:** 내 물건에 댓글이 달리거나, 내 댓글에 답글이 달리면 알림 발송 (`NotificationType.ITEM_COMMENT`)
- **수정/삭제:** 본인이 작성한 댓글 관리

### 필요 작업
- `ItemComment` 엔티티 설계 (`itemId`, `writerId`, `content`, `parentId`, `isSecret`)
- `ItemCommentService` 및 Controller 구현
- `NotificationService` 연동

---

## 2. 🔍 검색 및 필터링 (Search & Filter)
물건 탐색 편의성을 높이기 위한 필수 기능입니다.

### 기능 명세
- **키워드 검색:** 제목 및 내용 검색
- **카테고리 필터:** `ItemCategory` 별 모아보기 (디지털, 가전, 가구 등)
- **상태 필터:** 판매중(`ON_SALE`)인 물건만 보기 / 판매완료 포함 보기 옵션
- **정렬:** 최신순(기본), 낮은 가격순, 높은 가격순

### 필요 작업
- `ItemRepository`에 QueryDSL 또는 JPQL 기반 동적 쿼리 구현 (`BooleanBuilder` 활용)
- `ItemSearchCondition` DTO 생성 (keyword, category, status, minPrice, maxPrice)
- Controller에 검색 파라미터 매핑 (`Pageable` 연동)

---

## 3. 👤 프로필 및 매너온도 (Profile & Reputation)
거래 상대방에 대한 신뢰도를 확인할 수 있는 기능입니다.

### 기능 명세
- **프로필 조회:** 사용자의 닉네임, 프로필 사진, 매너온도 확인
- **프로필 수정:** 닉네임 변경 및 프로필 이미지 업로드 (S3 활용)
- **판매 내역:** 해당 사용자가 판매 중인/판매 완료한 물건 목록 조회
- **매너온도 시스템:** 초기 36.5도 시작, 칭찬 받으면 상승, 비매너 신고 시 하락

### 필요 작업
- `Member` 엔티티에 `profileImageUrl`, `mannerTemperature` 필드 추가
- `ProfileService` 구현 (이미지 업로드 로직 포함)
- `Review` 도메인 설계 (매너 평가용)

---

## 4. 📍 대여 기능 (Rental)
사내/커뮤니티 내 물품 공유를 위한 특화 기능입니다.

### 기능 명세
- **대여/반납 프로세스:**
    1. 대여 요청 (`RENTAL_REQUEST`)
    2. 승인 및 수령 (`RENTAL_START`)
    3. 반납 예정일 알림
    4. 반납 완료 (`RENTAL_RETURNED`)
- **기간 설정:** 물건 등록 시 최대 대여 가능 기간 설정
- **연체 관리:** 반납 기한 초과 시 알림 및 페널티(매너온도 하락 등)

### 필요 작업
- `ItemType`에 `RENTAL` 추가
- `Reservation` 로직을 확장하거나 별도 `Rental` 도메인 분리 고려
- Scheduler를 이용한 반납 기한 체크 로직 (`@Scheduled`)

---

## 5. 📢 알림 시스템 고도화 (Advanced Notification System)
단순한 인앱 알림 목록 조회를 넘어, 실시간성과 편의성을 강화하는 기능입니다.

### 5.1 키워드 알림 (Keyword Notification)
사용자가 등록한 관심 키워드에 해당하는 물건이 올라오면 즉시 알려주는 기능입니다.

#### 상세 기능 명세
- **키워드 관리:**
  - 키워드 등록/삭제 (최대 10개 제한)
  - 키워드 매칭 옵션 (정확히 일치, 포함 등)
- **매칭 프로세스:**
  - **이벤트 기반:** `ItemCreatedEvent` 발생 시 비동기 큐(`Event Listener` or `Message Queue`)로 전달
  - **필터링:** 등록된 전체 키워드 중 제목/본문과 매칭되는 사용자 리스트 추출
  - **중복 방지:** 동일 물건에 대해 여러 키워드가 매칭되어도 알림은 1회만 발송
- **성능 고려:**
  - 키워드 매칭 로직이 메인 트랜잭션을 방해하지 않도록 철저히 분리 (`@Async`)
  - Aho-Corasick 알고리즘이나 ElasticSearch 사용 고려 (데이터 증가 시)

### 5.2 실시간 웹 알림 (Real-time Web Push)
새로고침 없이 알림을 즉시 수신하는 기능입니다.

#### 상세 기능 명세
- **SSE (Server-Sent Events) 도입:**
  - 클라이언트가 접속 시 `SseEmitter` 구독
  - 알림 발생 시 서버에서 클라이언트로 이벤트 푸시
- **연결 관리:**
  - Timeout 및 재연결 처리 (Heartbeat)
  - 다중 탭 접속 시 처리 방안 고려

### 5.3 모바일/브라우저 푸시 (FCM Integration)
앱을 켜두지 않아도 알림을 받을 수 있는 기능입니다.

#### 상세 기능 명세
- **FCM (Firebase Cloud Messaging) 연동:**
  - 사용자 기기별 FCM 토큰 관리 (`DeviceToken` 엔티티 필요)
  - 웹 워커(Service Worker)를 이용한 백그라운드 푸시 알림
- **푸시 내용 보안:**
  - 민감 정보 제외하고 "새 알림이 있습니다" 형태로 발송하거나 Payload 암호화

### 5.4 알림 설정 (Notification Settings)
사용자가 원하는 알림만 받을 수 있도록 제어권을 제공합니다.

#### 상세 기능 명세
- **알림 유형별 ON/OFF:**
  - 키워드 알림
  - 댓글/채팅 알림
  - 예약 관련 알림
  - 마케팅 정보 알림
- **방해 금지 모드:**
  - 특정 시간대(예: 23:00 ~ 07:00) 알림 수신 거부 설정

---

## 6. 🛠 기술적 과제 (Technical Challenges)
위 기능들을 구현하기 위해 해결해야 할 기술적 과제들입니다.

- **WebSocket vs SSE:** 단방향 알림에는 SSE가 적합하나, 추후 채팅 기능을 고려하면 WebSocket으로 통합할지 결정 필요
- **동시성 처리:** 인기 키워드 등록 시 대량의 알림 생성(`fan-out`) 부하 분산 전략
- **분산 환경:** 서버가 여러 대로 확장될 경우, 실시간 세션 공유 문제 (Redis Pub/Sub 활용 필요)
