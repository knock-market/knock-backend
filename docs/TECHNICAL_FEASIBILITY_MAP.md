# Knock Technical Feasibility Map

Updated: 2026-06-02

## Scope and safety

This map checks whether the current repository can support the Knock Market product direction across frontend, backend, API, and docs surfaces. It intentionally avoids secret-bearing files such as `.env*`, credentials, private keys, and runtime-only secret values.

## Executive assessment

Overall feasibility is **high** for continuing the personal seller shelf / share-link marketplace direction. The main code paths already exist: public marketplace and item detail, seller shops and share links, item creation with pickup location, reservations, notifications, bookmarks, reviews, image upload, and profile/settings. The highest-risk areas are not missing modules; they are runtime configuration, external Naver/S3 dependencies, and keeping generated/manual API docs synchronized.

## Feasibility matrix

| Area | Current evidence | Feasibility | Key risks / next checks |
|---|---|---:|---|
| Product contract | `docs/REQUEST-v2.0.md` defines the move from group market to personal seller shelves, public item `publicId`, expiring share links, reservation intent, and event notifications. | High | Keep group/category/block/reputation removals out of new requirements unless explicitly reintroduced. |
| Backend module shape | `knock-backend/settings.gradle.kts` includes `core:core-api`, `core:core-auth`, `core:core-enum`, `storage:db-core`, `storage:memory`, `infra:s3`, `tests:api-docs`, and support modules. | High | New work should stay inside the existing controller/service/repository module boundaries from `docs/BACKEND_CONVENTION.md`. |
| Public/private API auth | `SecurityConfig` permits `GET /api/v1/items`, public `GET /api/v1/items/{uuid}`, public `GET /api/v1/members/{memberId}/items`, and public seller-share token reads while protecting owner/action APIs. | High | Public response DTOs must continue to expose only seller public profile, item public fields, and trade location. |
| Item/catalog flow | `ItemController` and `ItemService` support item creation, public detail by `publicId`, marketplace list, seller list, owner management detail, delete, trade-location validation, and Redis-backed view dedup. | High | Item actions still mix public IDs for browsing and internal IDs for management/reservation/bookmark actions; frontend navigation must preserve the correct identifier. |
| Seller share flow | `SellerShareController`, `SellerShareService`, and `SellerShareLink` support one active link per seller, secure URL-safe tokens, optional expiry, click/use counters, deactivation, and public shop reads. | High | Time behavior depends on the configured server `Clock`; link stats and availability should be covered when changing share-link semantics. |
| Reservation and notifications | `ReservationController`, `ReservationService`, `NotificationService`, `Reservation`, and `Notification` cover create/approve/complete/cancel, duplicate prevention, ownership checks, and notification fanout. | Medium-high | Reservation create request validation is lighter than item/review validation; preserve transaction/locking behavior when adding states or side effects. |
| Reviews | `ReviewController`, `ReviewService`, and `Review` tie reviews to completed reservations with duplicate prevention and score validation at the request boundary. | High | Any richer review feature should verify completed-reservation ownership and duplicate rules in service tests. |
| Location search and maps | `LocationController`, `LocationSearchService`, `NaverLocationClient`, `NaverLocationProperties`, `PickupLocationPicker`, `NaverMap`, and `utils/naverMap.ts` support Naver-backed pickup location search and map display. | Medium | External credentials and network access are required at runtime; missing backend Naver settings or frontend `VITE_NAVER_MAP_CLIENT_ID` should degrade clearly without logging secrets. |
| Images | `ImageController`, `ImageService`, `infra:s3`, and `imagesApi.upload/delete` support multipart upload and image delete. | Medium | S3 runtime config is external; local/offline QA may need mocks or non-production credentials. |
| Frontend route/auth shape | `knock-frontend/App.tsx` defines public routes for onboarding, home, login/signup, terms/privacy, public item UUIDs, seller pages, and share shops; other routes are guarded by `authApi.getMe()`. | High | Hash-router paths and login `next` redirects need browser QA after changes to public/protected route policy. |
| Frontend API client | `knock-frontend/services/client.ts`, `services/index.ts`, and `types.ts` centralize `/api/v1` Axios calls, session cookies, response unwrapping, and typed DTOs for all major endpoint families. | High | API DTO changes must update TypeScript types and affected pages together. |
| Docs and generated API evidence | `docs/API_REFERENCE.md` covers auth, members, items, bookmarks, reservations, notifications, reviews, images, locations, and seller shares. `core-api/src/docs/asciidoc/index.adoc` and REST Docs snippets exist. | Medium-high | Manual docs can drift from generated snippets; API changes should run Asciidoctor and update both docs and frontend contracts when applicable. |
| Full-stack verification harness | `docs/HARNESS.md`, `harness/harness.yml`, and `scripts/harness.sh` exist; `knock-frontend` scripts provide `typecheck`, `test`, `lint`, and `build`; backend Gradle tasks include `test`, `check`, and `core:core-api:asciidoctor`. | High | E2E browser QA still depends on local services and external/runtime config. |

## Ownership boundaries for future work

- **Frontend UI/client:** `knock-frontend/App.tsx`, `knock-frontend/pages/*`, `knock-frontend/components/*`, `knock-frontend/services/*`, `knock-frontend/types.ts`, `knock-frontend/utils/*`.
- **Backend API/controller layer:** `knock-backend/core/core-api/src/main/java/com/knock/core/api/controller/v1/*` and request/response DTO packages.
- **Backend domain services:** `knock-backend/core/core-api/src/main/java/com/knock/core/domain/*`.
- **Persistence:** `knock-backend/storage/db-core/src/main/java/com/knock/storage/db/core/*`.
- **Auth/session:** `knock-backend/core/core-auth/*` plus `SecurityConfig` in `core-api`.
- **External integrations:** Naver location/map search under `core/domain/location` and frontend Naver map utilities; S3 image upload under `infra/s3` and `core/domain/image`.
- **Docs/API contract:** root `docs/*`, `knock-backend/core/core-api/src/docs/asciidoc/index.adoc`, and REST Docs tests/snippets.

## Recommended next feasibility checks

1. For each new product requirement, classify it as public browsing, authenticated buyer action, authenticated seller action, or operational/admin behavior before editing security rules.
2. Update backend request/response DTOs, frontend `types.ts`, frontend service wrappers, and `docs/API_REFERENCE.md` in the same change when an API contract changes.
3. Use targeted backend tests for controller auth policy, service invariants, and repository behavior; use frontend typecheck/build for client contract regressions.
4. Treat Naver and S3 as runtime dependencies: verify missing/invalid config behavior without printing secret values, then perform full QA only in an environment with approved non-production credentials.
5. Re-run REST Docs generation for API-shape changes and compare manual API reference text against generated snippets.

## Subagent evidence integrated

- Backend/API probe (`019e873d-989e-7453-bcb4-9d05e6d1c474`) mapped Gradle modules, controllers, domain services, data models, docs, and Naver/share-link risks.
- Frontend/docs probe (`019e873d-b02a-76c0-869a-892d304e96b5`) mapped public/protected routes, typed service wrappers, frontend feature pages, docs coverage, generated snippets, and runtime configuration risks.
- Serial searches before spawn: 0.
- Secret safety: probes and local inspection avoided `.env*`, credential, private-key, and obvious secret-bearing files.
