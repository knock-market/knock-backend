# Knock API Reference

문서화된 모든 API는 `core-api` 모듈의 테스트를 통해 검증되었습니다.
상세한 Request/Response 스니펫은 `./gradlew :core:core-api:asciidoctor` 실행 후 생성되는 HTML 문서를 참고하세요.

## 1. Auth API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/login` | 일반 이메일 로그인 | `AuthLoginRequestDto` <br> `{ email, password }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| POST | `/api/v1/auth/logout` | 로그아웃 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |

## 2. Member API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/members` | 회원가입 | `MemberSignupRequestDto` <br> `{ email, name, password, nickname, profileImageUrl }` | `MemberSignupResponseDto` <br> `{ email, name, nickname, profileImageUrl }` | ✅ Implemented |
| GET | `/api/v1/members/my` | 내 정보 조회 | `(None)` | `MemberResponseDto` <br> `{ email, name, nickname, profileImageUrl }` | ✅ Implemented |

## 3. Group API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/groups` | 그룹 생성 | `GroupCreateRequestDto` <br> `{ name, description }` | `GroupIdResponseDto` <br> `{ id }` | ✅ Implemented |
| POST | `/api/v1/groups/{groupId}/invite-codes` | 초대코드 생성 | `InviteCodeRequestDto` <br> `{ duration }` | `GroupInviteCodeResponseDto` <br> `{ inviteCode, expiresAt }` | ✅ Implemented |
| POST | `/api/v1/groups/join` | 그룹 가입 (초대코드) | `GroupJoinRequestDto` <br> `{ inviteCode }` | `GroupIdResponseDto` <br> `{ id }` | ✅ Implemented |
| POST | `/api/v1/groups/{groupId}/leave` | 그룹 탈퇴 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/groups/my` | 내 그룹 목록 조회 | `(None)` | `List<GroupResponseDto>` <br> `[{ id, name, description }]` | ✅ Implemented |
| GET | `/api/v1/groups/{groupId}` | 그룹 상세 조회 | `(None)` | `GroupResponseDto` <br> `{ id, name, description }` | ✅ Implemented |

## 4. Item API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/items` | 상품 등록 | `ItemCreateRequestDto` <br> `{ groupId, title, description, price, itemType, category, imageUrls }` | `ItemIdResponseDto` <br> `{ id }` | ✅ Implemented |
| GET | `/api/v1/items/{itemId}` | 상품 상세 조회 | `(None)` | `ItemResponseDto` <br> `{ id, title, description, price, type, category, status, imageUrls, writerId }` | ✅ Implemented |
| GET | `/api/v1/groups/{groupId}/items` | 그룹 내 상품 목록 (피드) | `(None)` | `List<ItemSummaryResponseDto>` <br> `[{ id, title, price, type, category, status, thumbnailUrl, writerId }]` | ✅ Implemented |
| GET | `/api/v1/items/my-selling` | 내 판매 상품 목록 | `(None)` | `List<ItemSummaryResponseDto>` | ✅ Implemented |

## 5. Bookmark API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/items/{itemId}/bookmarks` | 상품 찜하기 (Toggle) | `(None)` | `BookmarkToggleResponseDto` <br> `{ itemId, isToggleOn }` | ✅ Implemented |
| GET | `/api/v1/items/my-bookmarks` | 내 찜 목록 | `(None)` | `List<MyBookmarkResponseDto>` <br> `[{ id, title, price, thumbnailUrl, createdAt }]` | ✅ Implemented |

## 6. Reservation API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/reservations` | 예약 신청 (구매 요청) | `ReservationCreateRequestDto` <br> `{ itemId }` | `Long` (reservationId) | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/approve` | 예약 승인 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/complete` | 거래 완료 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/cancel` | 예약 취소 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/items/{itemId}/reservations` | 상품별 예약 목록 (판매자용) | `(None)` | `List<ReservationResponseDto>` | ✅ Implemented |
| GET | `/api/v1/reservations/my` | 내 예약 내역 조회 | `(None)` | `List<ReservationResponseDto>` <br> `[{ id, itemId, itemTitle, memberId, memberName, status, createdAt }]` | ✅ Implemented |

## 7. Notification API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/notifications` | 알림 목록 조회 | `(None)` | `List<NotificationResponseDto>` <br> `[{ id, type, content, relatedUrl, isRead, createdAt }]` | ✅ Implemented |
| PATCH | `/api/v1/notifications/{id}/read` | 알림 읽음 처리 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |

## 8. Image API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/images/upload` | 이미지 업로드 | `multipart/form-data` <br> `file`, `directory` | `ImageUploadResult` <br> `{ originalFilename, imageUrl, s3Key }` | ✅ Implemented |
| DELETE | `/api/v1/images` | 이미지 삭제 | `QueryParam` <br> `imageUrl` | `ApiResponse` <br> `(void)` | ✅ Implemented |
