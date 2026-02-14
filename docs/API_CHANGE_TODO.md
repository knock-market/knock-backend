# API 변경 TODO

2026-02-14 기준, 프론트-백엔드 연동에 필요한 핵심 API 항목은 모두 반영되었습니다.

## 완료 항목

- `PUT /api/v1/members/my` (프로필 수정)
- `GET /api/v1/members/my/settings/notifications`
- `PUT /api/v1/members/my/settings/notifications`
- `GET /api/v1/members/my/blocked`
- `POST /api/v1/members/{memberId}/block`
- `DELETE /api/v1/members/{memberId}/block`
- `POST /api/v1/groups/join` 프론트 실연동
- `DELETE /api/v1/items/{itemId}` (상품 삭제)
- `GET /api/v1/members/my` 확장 (`id`, `mannerTemperature`)

## 현재 TODO

- 없음
