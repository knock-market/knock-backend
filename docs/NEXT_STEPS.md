# Knock Next Steps Tracker

업데이트 기준: 2026-05-29

## Problem 1-Pager

### 배경

루트 `docs` 문서는 Knock Market이 그룹형 장터에서 개인 판매자 매대와 공유 링크 중심 서비스로 전환되었음을 설명한다. 실제 코드도 `core-api`, `core-auth`, `storage/db-core`, `infra/s3`, `storage/memory` 중심으로 해당 방향을 대부분 반영하고 있다.

### 문제

문서에는 구현 완료로 기록된 기능이 많지만, 실제 운영 품질 관점에서는 공개 API 인증 정책, 조회수 집계, 시간 정책, 입력 검증, 문서/테스트 최신성처럼 다음 작업으로 추적해야 할 남은 경계가 있다.

### 목표

- 문서와 실제 코드가 일치하는 부분과 차이가 있는 부분을 분리한다.
- 다음 작업을 작고 안전한 단위로 추적한다.
- 각 작업의 근거 파일과 검증 방향을 남긴다.

### 비목표

- 이번 문서는 기능 구현을 포함하지 않는다.
- 운영 DB 마이그레이션을 직접 수행하지 않는다.
- 프론트엔드 변경은 별도 저장소 상태 확인 후 진행한다.

### 제약

- 비밀값은 문서와 로그에 남기지 않는다.
- API 동작 변경은 컨트롤러, 서비스, 보안 설정, 테스트, REST Docs를 함께 갱신한다.
- 시간 계산은 서버 기본 타임존에 의존하지 않도록 정책을 먼저 정한다.

## 확인한 현재 상태

- 멀티 모듈 구성은 `docs/MODULE.md`의 설명과 일치한다: `core:core-api`, `core:core-auth`, `core:core-enum`, `storage:db-core`, `storage:memory`, `infra:s3`, `clients:client-example`, `tests:api-docs`, `support:*`.
- 루트 하네스 문서와 실제 파일이 일치한다: `Makefile`, `scripts/harness.sh`, `harness/harness.yml`이 존재하고 `doctor`, `verify`, `backend:*`, `frontend:*`, `deps:*`, `docs:serve`를 제공한다.
- Google OAuth는 구현되어 있고 Kakao OAuth 엔드포인트는 없다. API Reference의 "Kakao Not Implemented" 상태와 일치한다.
- 그룹, 차단, 평판, 상품 카테고리 모델은 현재 검색 기준 주요 코드 경로에서 제거된 상태다.
- 개인 매대, 판매자 공유 링크, 상품 `publicId`, 거래 위치 필드는 코드에 반영되어 있다.
- `docs/BACKEND_CONVENTION.md`의 예외 항목은 아직 유효하다. 예를 들어 `ItemCreateData`가 controller request DTO를 참조하고, `ReviewService#getReviewList`가 API response DTO를 반환한다.

## 다음 작업 후보

### P0. 공개 마켓 목록 인증 정책 확정 - Done

- 근거: `docs/API_REFERENCE.md`는 `GET /api/v1/items`를 "전체 마켓 상품 목록"으로 설명하지만, `SecurityConfig`에는 해당 경로의 `permitAll` 규칙이 없다.
- 위험: 프론트 홈/공유 유입에서 비로그인 마켓 탐색을 기대하면 401이 발생할 수 있다.
- 선택지:
  - A. `GET /api/v1/items`를 공개 API로 허용한다. 장점: 제품 흐름과 자연스럽다. 단점: 공개 노출 필드 검토가 필요하다. 위험: 개인 정보가 포함되면 안 된다.
  - B. 로그인 필요 API로 유지하고 문서를 수정한다. 장점: 노출 위험이 작다. 단점: "전체 마켓" 경험과 어긋날 수 있다. 위험: 프론트 UX가 로그인 강제 흐름이 된다.
- 권장: A를 우선 검토한다. 응답 DTO가 공개 프로필과 상품 공개 정보 위주라 현재 제품 방향과 더 잘 맞는다.
- 검증: `SecurityConfig` 테스트 또는 컨트롤러 테스트에 비로그인 성공/실패 경로를 추가하고 REST Docs를 갱신한다.
- 결과: `GET /api/v1/items` 정확 경로만 공개 허용했다. `GET /api/v1/items/my-selling`과 `POST /api/v1/items`는 비로그인 접근을 계속 차단한다.

### P1. 조회수 집계 정책 정리 - Done

- 근거: `ItemService#increaseViewCount`와 `Item#viewCount`에 TODO가 남아 있었고, 기존 구현은 조회마다 DB 카운트를 증가시키는 상태였다.
- 위험: 새로고침, 봇, 같은 사용자 반복 조회로 지표가 과대 집계될 수 있다.
- 선택지:
  - A. Redis 기반 30분 중복 방지 키를 구현한다. 장점: 기존 주석 의도와 맞다. 단점: Redis 장애/TTL 정책을 테스트해야 한다. 위험: 비로그인 사용자의 식별 기준이 필요하다.
  - B. 단순 조회수 증가를 유지하고 문서에 "raw view"로 명시한다. 장점: 구현이 단순하다. 단점: 지표 신뢰도가 낮다. 위험: 판매자 지표로 오해될 수 있다.
- 권장: A를 설계하되, 비로그인은 세션 또는 익명 식별 정책을 먼저 정한다.
- 검증: 동일 사용자 반복 조회, 다른 사용자 조회, 비로그인 조회에 대한 단위/통합 테스트를 추가한다.
- 결과: Redis `setIfAbsent` + 30분 TTL로 로그인 사용자는 `memberId`, 비로그인 사용자는 `SESSION_ID` 기준 중복 조회를 방지한다. 판매자 본인 조회는 집계하지 않고, Redis 오류 시 상세 조회는 유지하며 조회수 집계만 생략한다.

### P1. 공유 링크 시간 정책 명시 - Done

- 근거: `SellerShareService`가 `LocalDateTime.now()`로 만료 시간을 계산한다. 컨벤션 문서도 시간대 정책 필요성을 언급한다.
- 위험: 서버 타임존, DST, 운영 환경 설정에 따라 만료 판단이 달라질 수 있다.
- 선택지:
  - A. `Clock`과 명시 ZoneId를 주입한다. 장점: 테스트가 결정적이고 정책이 드러난다. 단점: 생성자/테스트 보강이 필요하다. 위험: 기존 저장값 해석 정책을 정해야 한다.
  - B. 현행 `LocalDateTime.now()`를 유지하고 운영 서버 타임존을 KST로 고정한다. 장점: 변경이 작다. 단점: 코드만 보고 정책을 알기 어렵다. 위험: 환경 drift에 취약하다.
- 권장: A. 최소한 서비스 경계에서 `Clock`을 주입해 테스트 가능하게 만든다.
- 검증: `ONE_HOUR`, `ONE_DAY`, `PERMANENT`, 만료 직전/직후 테스트를 추가한다.
- 결과: `TimeConfig`에서 `ZoneId.of("Asia/Seoul")` 기반 `Clock`을 주입하고, 공유 링크 만료 생성/검사는 `LocalDateTime.now(clock)`으로 통일했다. DB/API의 `LocalDateTime` 형태는 유지하며 KST-local 정책을 코드와 테스트에 명시했다. `ONE_HOUR`, `ONE_DAY`, `PERMANENT`, null 기본값, `expiresAt == now` 경계, 만료 직후 click/use count 동작을 고정 테스트로 검증했다.

### P1. 운영 DB 마이그레이션 계획 작성 - Done

- 근거: `docs/REQUEST-v2.0.md`는 운영 DB 마이그레이션 자동화를 비목표로 두고, 다음 후보에 제거 스크립트 작성을 남겼다.
- 위험: 로컬 스키마 재생성 기준 변경이 운영 DB에 그대로 반영되지 않을 수 있다.
- 선택지:
  - A. Flyway/Liquibase 도입 후 마이그레이션을 코드화한다. 장점: 재현 가능하다. 단점: 초기 도입 비용이 있다. 위험: 기존 운영 상태 조사가 필요하다.
  - B. SQL 운영 절차서를 먼저 작성한다. 장점: 빠르게 위험을 드러낸다. 단점: 자동화가 약하다. 위험: 수동 실행 실수 가능성이 있다.
  - C. SQL 절차서로 1회 운영 실사/스테이징 검증을 끝낸 뒤, 확인된 스키마를 Flyway/Liquibase baseline으로 삼는다. 장점: 현재 불확실성을 낮추면서 이후 재현성을 확보한다. 단점: 수동 절차와 도구 도입을 모두 관리해야 한다.
- 권장: C에 가까운 단계적 접근. 먼저 `docs/OPERATING_DB_MIGRATION_PLAN.md`의 expand → backfill/verify → compatible deploy → contract 절차로 운영 스키마 차이를 확인하고, 제거 DDL은 백업/아카이브와 롤링 배포 안정화 이후에 실행한다.
- 검증: 운영과 동일한 스테이징/복제본에서 메타데이터 실사, `public_id` backfill/unique 검증, Hibernate `ddl-auto=validate` 부팅, 상품/공유 링크/예약/알림 smoke test를 통과해야 한다.
- 결과: `docs/OPERATING_DB_MIGRATION_PLAN.md`에 제거 대상(`item.group_id`, `item.category`, `member.manner_temperature`, `member_block`, 그룹 테이블), 현재 코드가 요구하는 스키마, 위험, 선택지, 권장 절차, 검증/롤백/중단 기준을 문서화했다.

### P2. DTO 레이어 의존 정리

- 근거: `docs/BACKEND_CONVENTION.md`의 "현재 코드에서 확인된 예외/개선 권장" 항목이 여전히 재현된다.
- 위험: 도메인 DTO가 API request/response에 묶여 모듈 경계가 흐려진다.
- 선택지:
  - A. 신규 작업에서만 컨벤션을 지킨다. 장점: 변경 부담이 작다. 단점: 예외가 오래 남는다. 위험: 패턴이 계속 복제될 수 있다.
  - B. 작은 단위로 기존 예외를 정리한다. 장점: 구조가 명확해진다. 단점: 테스트/REST Docs 영향이 있다. 위험: 단순 이동 변경이 넓어질 수 있다.
- 권장: B를 하되 `ItemCreateData`, `ReviewService#getReviewList`처럼 문서에 언급된 항목부터 작게 처리한다.
- 검증: 관련 서비스/컨트롤러 단위 테스트와 REST Docs 테스트를 실행한다.

### P2. 입력 검증 일관화

- 근거: 일부 request DTO는 `@Valid`와 Bean Validation을 사용하지만, 상품 등록/이미지/리뷰 등은 서비스 검증 또는 직접 사용이 섞여 있다.
- 위험: 잘못된 입력이 서비스 깊은 곳까지 들어가거나 오류 메시지가 일관되지 않을 수 있다.
- 선택지:
  - A. Controller request DTO에 Bean Validation을 확장한다. 장점: HTTP 경계에서 빠르게 차단한다. 단점: 서비스 재사용 시 별도 검증이 필요하다.
  - B. Service 검증으로 통일한다. 장점: 유스케이스 경계가 명확하다. 단점: HTTP 오류 필드 메시지 품질이 낮아질 수 있다.
- 권장: HTTP 형식 검증은 DTO, 도메인 규칙은 Service로 나눈다.
- 검증: 성공 경로와 실패 경로 REST Docs/컨트롤러 테스트를 추가한다.

### P3. 문서 최신성 보강

- 근거: `docs/REQUEST-v1.1.md`는 archived이지만 최신 후보 작업과 겹치는 표현이 있고, `docs/API_REFERENCE.md`는 REST Docs HTML 생성 결과와 수동 표를 함께 참조한다.
- 위험: 구현 변경 후 수동 문서가 뒤처질 수 있다.
- 선택지:
  - A. `NEXT_STEPS.md`에서 작업 상태만 추적한다. 장점: 변경이 작다. 단점: API 표 최신성은 별도 관리가 필요하다.
  - B. API Reference에 "검증 명령/마지막 테스트 결과" 섹션을 추가한다. 장점: 최신성 판단이 쉬워진다. 단점: 매번 갱신해야 한다.
- 권장: A를 유지하면서 API 변경이 있을 때만 B를 적용한다.
- 검증: `./gradlew :core:core-api:asciidoctor` 생성 결과와 수동 표를 비교한다.

## 완료로 판단한 항목

- 루트 하네스 파일과 문서 정합성 확인.
- 개인 판매 매대 전환 관련 핵심 백엔드 구조 확인.
- Google OAuth 구현 및 Kakao 미구현 상태 확인.
- 그룹/차단/평판/카테고리 제거 상태 확인.

## 추적 규칙

- 새 작업을 시작할 때 이 문서의 후보를 하나만 선택한다.
- 선택한 작업은 Problem 1-Pager, 영향 파일, 테스트 계획을 먼저 적는다.
- 구현 후 `docs/API_REFERENCE.md`, `docs/MODULE.md`, REST Docs, 테스트 상태 중 변경이 필요한 항목을 같이 갱신한다.
