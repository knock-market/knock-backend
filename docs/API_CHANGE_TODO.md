# API 변경 TODO

2026-02-16 기준, 프론트-백엔드 연동에 필요한 핵심 API 항목은 모두 반영되었습니다.

## 완료 항목

- `PUT /api/v1/members/my` (프로필 수정)
- `GET /api/v1/members/my/settings/notifications`
- `PUT /api/v1/members/my/settings/notifications`
- `GET /api/v1/members/my/blocked`
- `POST /api/v1/members/{memberId}/block`
- `DELETE /api/v1/members/{memberId}/block`
- `POST /api/v1/groups/join` 프론트 실연동
- `DELETE /api/v1/items/{itemId}` (상품 삭제 + 활성 예약 자동 취소)
- `GET /api/v1/members/my` 확장 (`id`, `mannerTemperature`)
- `PATCH /api/v1/notifications/read-all` (알림 전체 읽음 처리)
- `GET /api/v1/items/my-bookmarks` 응답 확장 (`type`, `category`, `status`, `itemCreatedAt`)
- `POST /api/v1/members/{memberId}/block` 동시 요청 멱등 처리(유니크 충돌 허용)

## 현재 TODO

- 없음
