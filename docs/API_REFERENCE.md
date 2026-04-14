# Knock API Reference

업데이트 기준: 2026-04-14

문서화된 모든 API는 `core-api` 모듈의 테스트를 통해 검증되었습니다.
상세한 Request/Response 스니펫은 `./gradlew :core:core-api:asciidoctor` 실행 후 생성되는 HTML 문서를 참고하세요.
인증 방식은 **세션 쿠키 기반**이며, 소셜 로그인 API는 아직 구현되지 않았습니다.

자세한 내용은 [asciidoc 문서](../knock-backend/core/core-api/src/docs/asciidoc/index.adoc)를 참고하세요.

## 1. Auth API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/login` | 일반 이메일 로그인 | `AuthLoginRequestDto` <br> `{ email, password }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| POST | `/api/v1/auth/logout` | 로그아웃 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/auth/social/google/start` | 구글 OAuth 인가 시작(302 Redirect) | `QueryParam`(Optional) <br> `{ next }` | `302 Location: Google OAuth URL` | ✅ Implemented |
| GET | `/api/v1/auth/social/google/callback` | 구글 OAuth 콜백 로그인 | `QueryParam` <br> `{ code, state }` | `302 Location: next(있으면) 또는 GOOGLE_LOGIN_SUCCESS_REDIRECT_URI` | ✅ Implemented |
| POST | `/api/v1/auth/social/kakao` | 카카오 소셜 로그인 | `(TBD)` | `(TBD)` | ❌ Not Implemented |

## 2. Member API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/members` | 회원가입 | `MemberSignupRequestDto` <br> `{ email, name, password, nickname, profileImageUrl }` | `MemberSignupResponseDto` <br> `{ email, name, nickname, profileImageUrl, provider }` | ✅ Implemented |
| GET | `/api/v1/members/my` | 내 정보 조회 | `(None)` | `MemberResponseDto` <br> `{ id, email, name, nickname, profileImageUrl, provider, mannerTemperature }` | ✅ Implemented |
| PUT | `/api/v1/members/my` | 내 정보 수정 | `MemberUpdateRequestDto` <br> `{ nickname, profileImageUrl }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/members/my/settings/notifications` | 내 알림 설정 조회 | `(None)` | `NotificationSettingsResponseDto` <br> `{ push, newItems, chat, marketing, sound }` | ✅ Implemented |
| PUT | `/api/v1/members/my/settings/notifications` | 내 알림 설정 수정 | `NotificationSettingsUpdateRequestDto` <br> `{ push, newItems, chat, marketing, sound }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/members/my/blocked` | 차단 유저 목록 조회 | `(None)` | `List<BlockedMemberResponseDto>` <br> `[{ id, name, blockedAt }]` | ✅ Implemented |
| POST | `/api/v1/members/{memberId}/block` | 유저 차단 (멱등) | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| DELETE | `/api/v1/members/{memberId}/block` | 유저 차단 해제 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/members/{memberId}/items` | 특정 회원의 공개 판매 상품 목록 | `(None)` | `List<ItemSummaryResponseDto>` <br> `[{ id, title, price, type, category, status, thumbnailUrl, writerId, writerNickname, writerProfileImageUrl, likesCount, postedAt, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }]` | ✅ Implemented |
| POST | `/api/v1/seller-shares` | 내 판매 페이지 공유 링크 생성 | `SellerShareLinkCreateRequestDto` <br> `{ duration }` | `SellerShareLinkResponseDto` <br> `{ token, path, expiresAt }` | ✅ Implemented |
| GET | `/api/v1/seller-shares/{token}` | 공유 링크로 판매 페이지 조회 | `(None)` | `SellerShopResponseDto` <br> `{ sellerId, sellerName, sellerNickname, sellerProfileImageUrl, items }` | ✅ Implemented |

## 3. Group API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/groups` | 그룹 생성 | `GroupCreateRequestDto` <br> `{ name, description, imageUrl }` | `GroupCreateResponseDto` <br> `{ id, name, description, memberCount, profileImageUrl, inviteCode }` | ✅ Implemented |
| POST | `/api/v1/groups/{groupId}/invite-codes` | 초대코드 생성 | `InviteCodeRequestDto` <br> `{ duration }` | `GroupInviteCodeResponseDto` <br> `{ inviteCode, expiresAt }` | ✅ Implemented |
| POST | `/api/v1/groups/join` | 그룹 가입 (초대코드) | `GroupJoinRequestDto` <br> `{ inviteCode }` | `GroupIdResponseDto` <br> `{ id }` | ✅ Implemented |
| POST | `/api/v1/groups/{groupId}/leave` | 그룹 탈퇴 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/groups/my` | 내 그룹 목록 조회 | `(None)` | `List<GroupResponseDto>` <br> `[{ id, name, description, memberCount, profileImageUrl }]` | ✅ Implemented |
| GET | `/api/v1/groups/{groupId}` | 그룹 상세 조회 | `(None)` | `GroupResponseDto` <br> `{ id, name, description, memberCount, profileImageUrl }` | ✅ Implemented |

## 4. Item API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/items` | 전체 마켓 상품 목록 | `(None)` | `List<ItemSummaryResponseDto>` | ✅ Implemented |
| POST | `/api/v1/items` | 상품 등록 | `ItemCreateRequestDto` <br> `{ groupId?, title, description, price, itemType, category?, imageUrls, tradeLocationName?, tradeLocationAddress?, tradeLatitude?, tradeLongitude? }` | `ItemIdResponseDto` <br> `{ id }` | ✅ Implemented |
| GET | `/api/v1/items/{itemId}` | 상품 상세 조회 | `(None)` | `ItemResponseDto` <br> `{ id, title, description, price, type, category, status, imageUrls, writerId, writerNickname, writerProfileImageUrl, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }` | ✅ Implemented |
| GET | `/api/v1/groups/{groupId}/items` | 그룹 내 상품 목록 (피드) | `(None)` | `List<ItemSummaryResponseDto>` <br> `[{ id, title, price, type, category, status, thumbnailUrl, writerId, writerNickname, writerProfileImageUrl, likesCount, postedAt, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }]` | ✅ Implemented |
| GET | `/api/v1/items/my-selling` | 내 판매 상품 목록 | `(None)` | `List<ItemSummaryResponseDto>` <br> `[{ id, title, price, type, category, status, thumbnailUrl, writerId, writerNickname, writerProfileImageUrl, likesCount, postedAt, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }]` | ✅ Implemented |
| DELETE | `/api/v1/items/{itemId}` | 내 상품 삭제 (활성 예약은 자동 취소) | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |

### Item location field rules

- `tradeLatitude`와 `tradeLongitude`는 둘 다 없거나 둘 다 있어야 합니다.
- 위도 범위는 `-90..90`, 경도 범위는 `-180..180`입니다.
- 프론트엔드는 `VITE_NAVER_MAP_CLIENT_ID`가 있으면 등록 화면에서 Naver Map 검색/지도 선택을 사용합니다.
- `groupId`가 없으면 서버는 판매자의 개인 그룹을 사용합니다.
- `category`가 없으면 서버는 기본 `ETC`로 저장합니다.

## 5. Bookmark API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/items/{itemId}/bookmarks` | 상품 찜하기 (Toggle) | `(None)` | `BookmarkToggleResponseDto` <br> `{ itemId, toggleOn }` | ✅ Implemented |
| GET | `/api/v1/items/my-bookmarks` | 내 찜 목록 | `(None)` | `List<MyBookmarkResponseDto>` <br> `[{ bookmarkId, itemId, title, price, type, category, status, thumbnailUrl, createdAt, itemCreatedAt }]` | ✅ Implemented |

## 6. Reservation API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/reservations` | 예약 신청 (구매 요청) | `ReservationCreateRequestDto` <br> `{ itemId }` | `ReservationCreateResponseDto` <br> `{ reservationId }` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/approve` | 예약 승인 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/complete` | 거래 완료 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/cancel` | 예약 취소 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/items/{itemId}/reservations` | 상품별 예약 목록 (판매자용) | `(None)` | `List<ReservationResponseDto>` | ✅ Implemented |
| GET | `/api/v1/reservations/my` | 내 예약 내역 조회 | `(None)` | `List<ReservationResponseDto>` <br> `[{ id, itemId, itemTitle, memberId, memberName, status, createdAt }]` | ✅ Implemented |

## 7. Notification API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/notifications` | 알림 목록 조회 | `(None)` | `List<NotificationResponseDto>` <br> `[{ id, notificationType, content, relatedUrl, isRead, createdAt }]` | ✅ Implemented |
| PATCH | `/api/v1/notifications/{id}/read` | 알림 읽음 처리 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/notifications/read-all` | 내 알림 전체 읽음 처리 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
## 8. Image API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/images/upload` | 이미지 업로드 | `multipart/form-data` <br> `file`, `directory` | `ImageUploadResult` <br> `{ originalFilename, imageUrl, s3Key }` | ✅ Implemented |
| DELETE | `/api/v1/images` | 이미지 삭제 | `QueryParam` <br> `imageUrl` | `ApiResponse` <br> `(void)` | ✅ Implemented |
