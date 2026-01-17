# Knock Market 개발 가이드 및 구현 Task (Master Guide)

## 1. 서비스 개요 (Service Overview)
> **“모르는 사람 말고, 내가 아는 사람들끼리만 쓰는 작은 당근마켓”**
> 학교, 회사, 동네 친구들 등 **폐쇄형 그룹** 내에서 안 쓰는 물건을 가볍게 나눔/거래할 수 있는 서비스입니다.

*   **핵심 가치**: 심리적 부담 최소화, "예약 버튼" 중심의 간편 거래, 신뢰 기반 관계.
*   **주요 기능**: 그룹 생성/초대, 물건 등록(나눔/판매), 예약 시스템, 알림.

---

## 2. 시스템 아키텍처 (Architecture)
본 프로젝트는 **멀티 모듈 Gradle** 프로젝트로 구성되어 있습니다.

| 모듈 그룹 | 모듈 이름 | 역할 | 비고 |
|---|---|---|---|
| **Core** | `core:core-api` | **API 서버 (Web Layer)**. Controller, Service 로직 위치. | 실행 가능한 Boot JAR 생성 |
| | `core:core-auth` | **인증/인가**. Spring Security, JWT, Session 관리. | |
| | `core:core-enum` | 공통 Enum 클래스. | 순환 참조 방지용 최하위 모듈 |
| **Storage** | `storage:db-core` | **JPA/DB Layer**. Entity, Repository 위치. | |
| | `storage:memory` | Redis 등 인메모리 저장소 설정. | |
| **Infra** | `infra:s3` | AWS S3 파일 업로드/다운로드. | |
| **Clients** | `clients:*` | 외부 API 연동 (Feign Client). | |

---

## 3. 현재 구현 상태 (Current Status)
**(2026-01-17 기준)**

### ✅ 구현 완료 (Implemented)
*   **Auth**: 이메일/비밀번호 기반 로그인/회원가입 (`SessionAuthService`).
*   **Group**: 그룹 생성, 초대코드 발급/검증, 가입/탈퇴.
*   **Item**: 상품 등록(이미지 포함), 상세 조회, 그룹별 피드 조회.
*   **Reservation**: 예약 신청(동시성 제어 포함), 승인, 완료, 취소.
*   **Notification**: 알림 생성 및 목록 조회 (DB 기반).

### ⚠️ 미구현 / 개선 필요 (To-Do)
*   **Social Auth**: 카카오/구글 로그인 미구현 (우선순위 높음).
*   **User Profile**: 프로필 수정, 타인 프로필 조회, 매너온도.
*   **Interactive Features**: 찜하기(Bookmark), 후기(Review) 시스템 부재.
*   **Real-time**: SSE(Server-Sent Events) 알림 전송 미구현.
*   **Search**: 검색 및 필터링 기능 부족.

---

## 4. 상세 구현 Task (Immediate Tasks)
**현재 우선적으로 진행해야 할 MVP 고도화 작업 목록입니다.**

### Issue 1: 엔티티 및 DB 스키마 고도화
*   [ ] **Member**: `providerId` (String, 소셜 ID), `mannerTemperature` (Double, 기본 36.5) 컬럼 추가.
*   [ ] **Item**: `viewCount` (Long), `coverImage` (Group 엔티티) 컬럼 추가.
*   [ ] **Bookmark**: 신규 엔티티 생성 (`Member` - `Item` N:M 매핑).
*   [ ] **Review**: 신규 엔티티 생성 (`Reservation` - `Member` - `Content` - `Score`).

### Issue 2: 소셜 로그인 및 회원 기능
*   [ ] **OAuth Client**: `clients` 모듈 또는 `core-auth`에 Kakao/Google 연동 구현 (Feign 또는 WebClient).
*   [ ] **Social Login API**: `POST /api/v1/auth/social/{provider}`. 기존 회원 연동 로직 포함.
*   [ ] **Profile Update**: `PUT /api/v1/members/my`. 닉네임 변경 및 프로필 이미지 업로드 (`ImageService` 재사용).

### Issue 3: 상품 인터랙션 (Interaction)
*   [ ] **Bookmark API**: `POST /api/v1/items/{itemId}/bookmarks` (Toggle 방식).
*   [ ] **My Bookmarks**: `GET /api/v1/items/my-bookmarks` (내가 찜한 목록).
*   [ ] **My Selling**: `GET /api/v1/items/my-selling` (내 판매 내역).
*   [ ] **View Count**: 상품 상세 조회 시 조회수 증가 (Redis 캐싱 후 Write-back 권장).

### Issue 4: 후기 및 신뢰도 시스템
*   [ ] **Review API**: `POST /api/v1/reviews`. 거래 상태가 `COMPLETED`일 때만 작성 가능하도록 검증.
*   [ ] **Manner Temperature**: 후기 평점에 따른 매너온도 업데이트 로직 (Domain Event 사용 고려).
*   [ ] **User Reviews**: `GET /api/v1/members/{memberId}/reviews`.

### Issue 5: 실시간 알림 (SSE)
*   [ ] **SSE Subscribe**: `GET /api/v1/notifications/subscribe`. `SseEmitter` 관리.
*   [ ] **Event Listener**: 알림 생성 시점에 DB 저장과 동시에 SSE 이벤트 발송 로직 추가.

---

## 5. 도메인 모델 상세 가이드 (Domain Guide)

### 신규 엔티티: Review (후기)
```java
@Entity
public class Review extends BaseEntity {
    @ManyToOne(fetch = LAZY) Reservation reservation; // 1:1 매핑 권장
    @ManyToOne(fetch = LAZY) Member reviewer;
    @ManyToOne(fetch = LAZY) Member reviewee;
    
    @Column(columnDefinition = "TEXT")
    String content;
    
    Integer score; // 1~5
    
    // 매너 뱃지는 ElementCollection 또는 별도 테이블로 관리
    // @ElementCollection List<String> badges;
}
```

### 신규 엔티티: Bookmark (찜)
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "item_id"}))
public class Bookmark extends BaseEntity {
    @ManyToOne(fetch = LAZY) Member member;
    @ManyToOne(fetch = LAZY) Item item;
}
```

### DTO 요구사항 (Spec)
*   **SocialLoginRequestDto**: `code` (String), `redirectUri` (String).
*   **ReviewCreateRequestDto**: `reservationId`, `content`, `score`, `badges` (List).
*   **MemberProfileResponseDto**: `id`, `nickname`, `profileImageUrl`, `mannerTemperature`.

---

## 6. 향후 로드맵 (Future Roadmap)
**MVP 안정화 이후 진행할 기능입니다.** (상세: `FUTURE_FEATURES.md`)

*   **댓글 및 문의**: 상품에 대한 공개 댓글 및 비밀 댓글.
*   **검색 및 필터링**: QueryDSL 도입, 동적 쿼리(`ItemSearchCondition`), 카테고리/상태 필터.
*   **대여(Rental) 기능**: 기간 설정, 반납 알림, 연체 페널티.
*   **알림 고도화**: 키워드 알림, 모바일 푸시(FCM) 연동.
*   **배치 처리**: 오래된 알림 삭제, 통계 데이터 집계.

---

## 7. 업데이트 기록
*   **v1.0**: 초기 문서 생성.
*   **v1.1**: `PLAN.md`, `MODULE.md`, `FUTURE_FEATURES.md` 내용을 통합하여 마스터 가이드로 업데이트. (Current)
