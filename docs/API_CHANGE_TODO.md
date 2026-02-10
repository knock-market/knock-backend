# API 변경 TODO

프론트엔드에서 사용 중이지만 백엔드 API가 구현되지 않았거나 수정이 필요한 항목들입니다.

---

## 1. 프로필 통계 API

**프론트엔드 위치:** `Profile.tsx` (116-129줄)

**현재 상태:** 하드코딩된 값 (공유: 12, 받음: 8, 활성: 3)

**필요한 API:**
```
GET /api/v1/members/my/stats
```

**응답 예시:**
```json
{
  "shared": 12,      // 판매/나눔한 상품 수
  "received": 8,     // 받은 상품 수
  "active": 3        // 현재 등록 중인 상품 수
}
```

---

## 2. 알림 설정 API

**프론트엔드 위치:** `NotificationSettings.tsx`

**현재 상태:** 로컬 상태만 관리, 서버에 저장되지 않음

**필요한 API:**
```
GET  /api/v1/members/my/settings/notifications
PUT  /api/v1/members/my/settings/notifications
```

**요청/응답 예시:**
```json
{
  "push": true,           // 푸시 알림 활성화
  "newItems": true,       // 새 상품 등록 알림
  "chat": true,           // 채팅 메시지 알림
  "marketing": false,     // 마케팅 알림
  "sound": true           // 알림 소리
}
```

---

## 3. 차단 유저 API

**프론트엔드 위치:** `BlockedUsers.tsx`

**현재 상태:** Mock 데이터만 사용

**필요한 API:**
```
GET    /api/v1/members/my/blocked           // 차단 목록 조회
POST   /api/v1/members/{memberId}/block     // 유저 차단
DELETE /api/v1/members/{memberId}/block     // 차단 해제
```

**GET 응답 예시:**
```json
[
  {
    "id": 1,
    "name": "유저 이름",
    "blockedAt": "2024-01-12T10:00:00Z"
  }
]
```

---

## 4. 프로필 수정 API

**프론트엔드 위치:** `EditProfile.tsx`

**현재 상태:** 로컬 상태만 관리, 서버에 저장되지 않음

**필요한 API:**
```
PUT /api/v1/members/my
```

**요청 예시:**
```json
{
  "nickname": "새로운 닉네임",
  "profileImageUrl": "https://..."
}
```

---

## 5. 초대 코드로 그룹 가입

**프론트엔드 위치:** `Home.tsx` (그룹 가입 모달)

**현재 상태:** setTimeout으로 시뮬레이션 중

**기존 API:**
```
POST /api/v1/groups/join
```

**요청 예시:**
```json
{
  "inviteCode": "KNOCK2024"
}
```

> **참고:** `services/index.ts`에 API가 정의되어 있으나 `Home.tsx`에서 실제 연결되지 않음

---

## 6. 매너 점수 (Trust Score)

**프론트엔드 위치:** `Profile.tsx` (매너 점수 원형 그래프)

**현재 상태:** 하드코딩된 `CURRENT_USER.trustScore` 사용

**필요 작업:** `MemberResponseDto`에 `trustScore` 필드 추가

```java
// MemberResponseDto.java
private Integer trustScore;
```

---

## 요약

| 기능 | 우선순위 | 상태 |
|------|----------|------|
| 프로필 통계 | 높음 | 미구현 |
| 알림 설정 | 중간 | 미구현 |
| 차단 유저 | 중간 | 미구현 |
| 프로필 수정 | 높음 | 미구현 |
| 그룹 가입 | 낮음 | API 존재, 프론트 연결 필요 |
| 매너 점수 | 낮음 | DTO 필드 추가 필요 |
