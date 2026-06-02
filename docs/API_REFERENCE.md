# Knock API Reference

업데이트 기준: 2026-06-02

문서화된 API는 `core-api` 모듈의 컨트롤러와 DTO를 기준으로 검수했습니다.
상세한 Request/Response 스니펫은 `./gradlew :core:core-api:asciidoctor` 실행 후 생성되는 HTML 문서를 참고하세요.
인증 방식은 **세션 쿠키 기반**이며, Google OAuth 로그인은 구현되어 있고 Kakao 소셜 로그인은 아직 구현되지 않았습니다.

자세한 내용은 [asciidoc 문서](../knock-backend/core/core-api/src/docs/asciidoc/index.adoc)를 참고하세요.

## 1. Auth API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/login` | 일반 이메일 로그인 | `AuthLoginRequestDto` <br> `{ email, password }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| POST | `/api/v1/auth/logout` | 로그아웃 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/auth/social/google/start` | 구글 OAuth 인가 시작(302 Redirect). `next`가 있으면 서버 세션에 저장 | `QueryParam`(Optional) <br> `{ next }` | `302 Location: Google OAuth URL` | ✅ Implemented |
| GET | `/api/v1/auth/social/google/callback` | 구글 OAuth 콜백 로그인. `/start`에서 저장한 `next`를 세션에서 소비 | `QueryParam` <br> `{ code, state }` | `302 Location: 저장된 next 또는 GOOGLE_LOGIN_SUCCESS_REDIRECT_URI` | ✅ Implemented |
| POST | `/api/v1/auth/social/kakao` | 카카오 소셜 로그인 | `(TBD)` | `(TBD)` | ❌ Not Implemented |

> Google OAuth의 `next`는 콜백 URL의 쿼리 파라미터가 아니라 `/api/v1/auth/social/google/start` 요청에서 받은 값을 세션에 저장해 두었다가 콜백에서 사용하는 값입니다. 허용되는 `next`는 `/`로 시작하는 내부 경로이며 `//`, CR, LF 문자를 포함하면 기본 성공 리다이렉트 경로로 대체됩니다.

## 2. Member API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/members` | 회원가입 | `MemberSignupRequestDto` <br> `{ email, name, password, nickname, profileImageUrl }` | `MemberSignupResponseDto` <br> `{ email, name, nickname, profileImageUrl, provider }` | ✅ Implemented |
| GET | `/api/v1/members/my` | 내 정보 조회 | `(None)` | `MemberResponseDto` <br> `{ id, email, name, nickname, profileImageUrl, provider }` | ✅ Implemented |
| PUT | `/api/v1/members/my` | 내 정보 수정 | `MemberUpdateRequestDto` <br> `{ nickname, profileImageUrl }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/members/my/settings/notifications` | 내 알림 설정 조회 | `(None)` | `NotificationSettingsResponseDto` <br> `{ push, newItems, chat, marketing, sound }` | ✅ Implemented |
| PUT | `/api/v1/members/my/settings/notifications` | 내 알림 설정 수정 | `NotificationSettingsUpdateRequestDto` <br> `{ push, newItems, chat, marketing, sound }` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/members/{memberId}/items` | 특정 회원의 공개 판매 상품 목록 | `(None)` | `List<ItemSummaryResponseDto>` <br> `[{ id, publicId, title, price, type, status, thumbnailUrl, writerId, writerNickname, writerProfileImageUrl, likesCount, postedAt, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }]` | ✅ Implemented |
| POST | `/api/v1/seller-shares` | 내 판매 페이지 공유 링크 생성 | `SellerShareLinkCreateRequestDto` <br> `{ duration }` <br> `ONE_HOUR`, `ONE_DAY`, `PERMANENT` | `SellerShareLinkResponseDto` <br> `{ token, path, expiresAt, active, clickCount, useCount }` | ✅ Implemented |
| GET | `/api/v1/seller-shares/my` | 내 현재 공유 링크와 지표 조회 | `(None)` | `List<SellerShareLinkSummaryResponseDto>` <br> 최신 링크 0~1개 `[{ token, path, expiresAt, active, clickCount, useCount, createdAt }]` | ✅ Implemented |
| DELETE | `/api/v1/seller-shares/{token}` | 공유 링크 중단 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/seller-shares/{token}` | 공유 링크로 판매 페이지 조회 | `(None)` | `SellerShopResponseDto` <br> `{ sellerId, sellerName, sellerNickname, sellerProfileImageUrl, items }` | ✅ Implemented |

## 3. Item API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/items` | 전체 마켓 상품 목록. 비로그인 조회 가능 | `(None)` | `List<ItemSummaryResponseDto>` | ✅ Implemented |
| POST | `/api/v1/items` | 내 개인 매대에 상품 등록 | `ItemCreateRequestDto` <br> `{ title, description, price, itemType, imageUrls, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }` | `ItemIdResponseDto` <br> `{ id, publicId }` | ✅ Implemented |
| GET | `/api/v1/items/{itemPublicId}` | 상품 상세 조회. UUID 형식 공개 식별자는 비로그인 조회 가능, 예약/북마크 같은 액션은 인증 필요 | `(None)` | `ItemResponseDto` <br> `{ id, publicId, title, description, price, type, status, imageUrls, writerId, writerNickname, writerProfileImageUrl, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }` | ✅ Implemented |
| GET | `/api/v1/items/manage/{itemId}` | 판매자 관리 화면용 상품 상세 조회. 인증 필요 | `(None)` | `ItemResponseDto` <br> `{ id, publicId, title, description, price, type, status, imageUrls, writerId, writerNickname, writerProfileImageUrl, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }` | ✅ Implemented |
| GET | `/api/v1/items/my-selling` | 내 판매 상품 목록 | `(None)` | `List<ItemSummaryResponseDto>` <br> `[{ id, publicId, title, price, type, status, thumbnailUrl, writerId, writerNickname, writerProfileImageUrl, likesCount, postedAt, tradeLocationName, tradeLocationAddress, tradeLatitude, tradeLongitude }]` | ✅ Implemented |
| DELETE | `/api/v1/items/{itemId}` | 내 상품 삭제 (활성 예약은 자동 취소) | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |

### Item location field rules

- 상품 등록에는 `title`, `description`, `price`, `itemType`, `imageUrls`, `tradeLocationName`, `tradeLocationAddress`, `tradeLatitude`, `tradeLongitude`가 모두 필요합니다.
- `tradeLatitude`와 `tradeLongitude`는 상품 등록 요청에서 둘 다 필수이며, 도메인 검증에서도 한쪽만 누락된 좌표를 거부합니다.
- `price`는 0 이상이어야 합니다.
- 위도 범위는 `-90..90`, 경도 범위는 `-180..180`입니다.
- 프론트엔드는 `VITE_NAVER_MAP_CLIENT_ID`가 있으면 등록 화면에서 Naver Map 지도 선택을 사용합니다.
- 장소 검색은 백엔드 `Location API`가 Naver Local Search/Geocoding API를 호출해 좌표를 반환합니다.
- 상품 등록은 인증된 판매자의 개인 매대에 저장되며 `groupId`와 `category`를 받지 않습니다.
- 상품 상세 URL은 `/item/{publicId}`를 사용합니다. UUID 형식 `publicId` 상세 조회는 공유 유입을 위해 공개하고, 내부 숫자 `id`는 예약/북마크 같은 인증 후 API payload에서만 사용합니다.

### Item view count rules

- 상품 상세 조회는 Redis TTL 키로 30분 내 중복 조회를 방지한 뒤 조회수를 증가시킵니다.
- 로그인 사용자는 `memberId`, 비로그인 사용자는 `SESSION_ID` 기준으로 중복을 판단합니다.
- 판매자 본인이 자신의 상품을 조회한 경우 조회수에 포함하지 않습니다.
- Redis 오류가 발생해도 상세 조회 응답은 유지하고 조회수 집계만 생략합니다.

### Seller share rules

- 공유 링크 유효시간은 `ONE_HOUR`(1시간), `ONE_DAY`(24시간), `PERMANENT`(무제한)만 사용합니다.
- 공유 링크 만료 시간은 서버 애플리케이션의 `Asia/Seoul` Clock으로 계산한 KST-local `LocalDateTime` 값으로 저장/응답합니다.
- 판매자 매대의 공유 상태는 구글 드라이브의 일반 액세스처럼 현재 링크 1개 기준으로 관리합니다.
- 새 공유 링크를 만들면 기존 링크는 모두 비활성화되고, `GET /api/v1/seller-shares/my`는 최신 링크 0~1개만 반환합니다.
- `clickCount`는 공유 링크 요청 수, `useCount`는 활성 상태에서 실제 판매 페이지가 열린 횟수입니다.
- 판매자가 `DELETE /api/v1/seller-shares/{token}`을 호출하면 해당 링크는 즉시 비활성화됩니다.

## 4. Bookmark API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/items/{itemId}/bookmarks` | 상품 찜하기 (Toggle) | `(None)` | `BookmarkToggleResponseDto` <br> `{ itemId, toggleOn }` | ✅ Implemented |
| GET | `/api/v1/items/my-bookmarks` | 내 찜 목록 | `(None)` | `List<MyBookmarkResponseDto>` <br> `[{ bookmarkId, itemId, itemPublicId, title, price, type, status, thumbnailUrl, createdAt, itemCreatedAt }]` | ✅ Implemented |

## 5. Reservation API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/reservations` | 예약 신청 (구매 요청) | `ReservationCreateRequestDto` <br> `{ itemId }` | `ReservationCreateResponseDto` <br> `{ reservationId }` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/approve` | 예약 승인 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/complete` | 거래 완료 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/reservations/{id}/cancel` | 예약 취소 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| GET | `/api/v1/items/{itemId}/reservations` | 상품별 예약 목록 (판매자용) | `(None)` | `List<ReservationResponseDto>` | ✅ Implemented |
| GET | `/api/v1/reservations/my` | 내 예약 내역 조회 | `(None)` | `List<ReservationResponseDto>` <br> `[{ id, itemId, itemTitle, memberId, memberName, status, createdAt }]` | ✅ Implemented |

## 6. Notification API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/notifications` | 알림 목록 조회 | `(None)` | `List<NotificationResponseDto>` <br> `[{ id, notificationType, content, relatedUrl, isRead, createdAt }]` | ✅ Implemented |
| PATCH | `/api/v1/notifications/{id}/read` | 알림 읽음 처리 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |
| PATCH | `/api/v1/notifications/read-all` | 내 알림 전체 읽음 처리 | `(None)` | `ApiResponse` <br> `(void)` | ✅ Implemented |

## 7. Review API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/reviews` | 거래 후기 작성 | `ReviewCreateRequest` <br> `{ itemId, content, score }` (`score`: 1..5) | `ReviewResponse` <br> `{ id, content, score }` | ✅ Implemented |
| GET | `/api/v1/members/{memberId}/reviews` | 특정 회원의 후기 목록 조회 | `(None)` | `List<ReviewResponse>` <br> `[{ id, content, score }]` | ✅ Implemented |

## 8. Image API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/images/upload` | 이미지 업로드 | `multipart/form-data` <br> `file`, `directory` | `ImageUploadResult` <br> `{ originalFilename, imageUrl, s3Key }` | ✅ Implemented |
| DELETE | `/api/v1/images` | 이미지 삭제 | `QueryParam` <br> `imageUrl` | `ApiResponse` <br> `(void)` | ✅ Implemented |

## 9. Location API
| Method | URI | Description | Request Body | Response Body | Status |
|---|---|---|---|---|---|
| GET | `/api/v1/locations/search?query={query}` | 픽업 위치 검색. 서버가 Naver Local Search 결과를 반환하고 가능한 경우 Geocoding으로 WGS84 좌표를 보강 | `(None)` | `List<LocationSearchResponseDto>` <br> `[{ name, address, latitude?, longitude?, naverMapX?, naverMapY? }]` | ✅ Implemented |

### Location search environment rules

- 서버 환경변수 `NAVER_MAP_CLIENT_ID`, `NAVER_MAP_CLIENT_SECRET`가 필요합니다.
- 장소명/역명 검색에는 네이버 개발자 센터 Search API 키인 `NAVER_SEARCH_CLIENT_ID`, `NAVER_SEARCH_CLIENT_SECRET`가 필요합니다.
- `NAVER_MAP_CLIENT_SECRET`는 프론트엔드에 넣지 않습니다.
- Naver 개발자 센터/Search API와 Naver Cloud Platform/Maps Geocoding API 권한이 모두 필요합니다.
