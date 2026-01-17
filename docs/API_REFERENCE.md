# Knock API Reference

## 1. Auth API
| Method | URI | Description | Status |
|---|---|---|---|
| POST | `/api/v1/auth/login` | 일반 이메일 로그인 | ✅ Existing |
| POST | `/api/v1/auth/logout` | 로그아웃 | ✅ Existing |
| POST | `/api/v1/auth/social/{provider}` | 소셜 로그인 (Kakao, Google) | ⚠️ New |

## 2. Member API
| Method | URI | Description | Status |
|---|---|---|---|
| POST | `/api/v1/members` | 회원가입 | ✅ Existing |
| GET | `/api/v1/members/my` | 내 정보 조회 | ✅ Existing |
| GET | `/api/v1/members/{memberId}` | 타 사용자 프로필 조회 (판매자 정보) | ⚠️ New |
| PUT | `/api/v1/members/my` | 내 프로필 수정 (이미지, 닉네임) | ⚠️ New |
| GET | `/api/v1/members/my/trust-badges` | 내 매너 뱃지 및 후기 목록 조회 | ⚠️ New |

## 3. Group API
| Method | URI | Description | Status |
|---|---|---|---|
| POST | `/api/v1/groups` | 그룹 생성 | ✅ Existing |
| GET | `/api/v1/groups/my` | 내 그룹 목록 조회 | ✅ Existing |
| GET | `/api/v1/groups/{groupId}` | 그룹 상세 조회 | ✅ Existing |
| POST | `/api/v1/groups/join` | 그룹 가입 (초대코드/검색) | ✅ Existing |
| POST | `/api/v1/groups/{groupId}/leave` | 그룹 탈퇴 | ✅ Existing |
| POST | `/api/v1/groups/{groupId}/invite-codes` | 초대코드 생성 | ✅ Existing |

## 4. Item API
| Method | URI | Description | Status |
|---|---|---|---|
| POST | `/api/v1/items` | 상품 등록 | ✅ Existing |
| GET | `/api/v1/items/{itemId}` | 상품 상세 조회 | ✅ Existing |
| GET | `/api/v1/groups/{groupId}/items` | 그룹 내 상품 목록 (피드) | ✅ Existing |
| GET | `/api/v1/items/my-selling` | 내가 판매 중인 상품 목록 | ⚠️ New |
| PUT | `/api/v1/items/{itemId}` | 상품 수정 | ⚠️ New |
| DELETE | `/api/v1/items/{itemId}` | 상품 삭제 | ⚠️ New |
| POST | `/api/v1/items/{itemId}/bookmarks` | 상품 찜하기 (Toggle) | ⚠️ New |
| GET | `/api/v1/items/my-bookmarks` | 내가 찜한 상품 목록 | ⚠️ New |

## 5. Reservation API
| Method | URI | Description | Status |
|---|---|---|---|
| POST | `/api/v1/reservations` | 구매 요청 (예약 신청) | ✅ Existing |
| GET | `/api/v1/reservations/my` | 내 예약 내역 (구매/판매) | ✅ Existing |
| PATCH | `/api/v1/reservations/{id}/approve` | 예약 승인 (판매자) | ✅ Existing |
| PATCH | `/api/v1/reservations/{id}/complete` | 거래 완료 (판매자) | ✅ Existing |
| PATCH | `/api/v1/reservations/{id}/cancel` | 예약 취소 | ✅ Existing |

## 6. Review API (New)
| Method | URI | Description | Status |
|---|---|---|---|
| POST | `/api/v1/reviews` | 거래 후기 및 매너 평가 작성 | ⚠️ New |
| GET | `/api/v1/members/{memberId}/reviews` | 사용자별 후기 목록 조회 | ⚠️ New |

## 7. Notification API
| Method | URI | Description | Status |
|---|---|---|---|
| GET | `/api/v1/notifications` | 알림 목록 조회 | ✅ Existing |
| PATCH | `/api/v1/notifications/{id}/read` | 알림 읽음 처리 | ✅ Existing |
| GET | `/api/v1/notifications/subscribe` | 실시간 알림 구독 (SSE) | ⚠️ New |
