# Knock Runbook

업데이트 기준: 2026-06-29
상태: operational command and local harness source of truth

이 문서는 루트에서 `knock-backend`, `knock-frontend`, `docs`를 하나의 작업 단위로 다루기 위한 반복 실행 절차를 정리한다. 제품 요구사항은 `docs/SPEC.md`, 기술 참조는 `docs/REFERENCE.md`, 개발 관행은 `docs/PRACTICES.md`를 따른다.

## 1. Scope

### 포함

- 로컬 의존성 실행/중지
- 백엔드 실행과 테스트
- 프론트엔드 실행과 테스트
- 문서 로컬 서빙
- 루트 품질 게이트

### 제외

- 운영 DB 직접 변경
- 외부 배포
- 인증/credential이 필요한 실서비스 호출

## 2. Quick start

```bash
make doctor
make deps-up
make backend-run
```

기본 품질 게이트:

```bash
make verify
```

백엔드만 검증:

```bash
make backend-unit
make backend-test
```

## 3. Command reference

| 명령 | 설명 |
|---|---|
| `make doctor` | 필수 도구와 각 축의 경로를 확인한다. |
| `make deps-up` | `knock-backend/docker/local/docker-compose.yml`로 로컬 의존성을 실행한다. |
| `make deps-down` | 로컬 의존성을 종료한다. |
| `make backend-run` | `:core:core-api:bootRun`으로 API 서버를 실행한다. |
| `make backend-test` | 백엔드 전체 테스트를 실행한다. |
| `make backend-unit` | `unitTest` 태스크를 실행한다. |
| `make backend-context` | `contextTest` 태스크를 실행한다. |
| `./scripts/harness.sh backend:restdocs` | `restDocsTest` 태스크를 실행해 REST Docs 스니펫을 검증한다. |
| `make docs-serve` | 문서를 `http://localhost:8088`에서 제공한다. |
| `make frontend-install` | 프론트엔드 의존성을 설치한다. |
| `make frontend-run` | 프론트엔드 개발 서버를 실행한다. |
| `make frontend-test` | 프론트엔드 테스트를 실행한다. |
| `make verify` | `doctor`, 백엔드 단위 테스트, 프론트엔드 테스트를 순서대로 실행한다. |

`knock-frontend`가 있으면 프론트엔드 테스트까지 실행하고, 없으면 프론트엔드 테스트 단계는 안내 후 성공으로 건너뛴다. 백엔드와 문서만 받은 환경에서도 기본 검증을 진행하기 위함이다.

## 4. Harness files

- `harness/harness.yml`: 축, 경로, 명령, 헬스 체크 URL, 로컬 의존성을 선언하는 매니페스트다.
- `scripts/harness.sh`: 사람이 실행하는 실제 명령 라우터다.
- `Makefile`: 자주 쓰는 명령의 짧은 별칭을 제공한다.

현재 하네스는 세 파일 사이에 일부 중복이 있다. 새 명령을 추가할 때는 사람이 실행하는 실제 경로인 `scripts/harness.sh`와 `Makefile`을 우선 갱신하고, `harness/harness.yml`은 선언적 문서/매니페스트로 함께 맞춘다.

## 5. Frontend discovery

하네스는 다음 순서로 프론트엔드를 탐색한다.

1. 루트 내부 `knock-frontend`
2. 루트의 형제 디렉터리 `../knock-frontend`

둘 다 없으면 프론트엔드 관련 명령은 안내 메시지를 출력하고 성공 종료한다.

## 6. Extension rules

1. 새 축을 추가할 때는 먼저 하네스 매니페스트와 실제 라우터의 책임을 정한다.
2. 사람이 자주 실행할 명령만 `scripts/harness.sh`와 `Makefile`에 연결한다.
3. CI에서도 같은 명령을 재사용한다. 예: 백엔드 단위 테스트는 `make backend-unit`, 기본 품질 게이트는 `make verify`.
4. 외부 서비스가 필요한 검증은 `deps-up`에 붙이거나 별도 명령으로 분리한다.
5. credential이 필요한 검증은 자동 실행하지 않고, 필요한 환경과 안전 조건만 문서화한다.

## 7. Common local flows

### Backend API smoke

```bash
make doctor
make deps-up
make backend-run
```

### Backend regression

```bash
make backend-unit
make backend-context
./scripts/harness.sh backend:restdocs
```

친구 초대/그룹 전용 access gate를 변경한 경우 운영/검증 전 `docs/db/20260629_seller_access_member.sql`을 non-local DB에 적용해야 한다. `db-core.yml`의 non-local 기본값은 `ddl-auto=validate`이므로 테이블이 없으면 애플리케이션이 기동하지 않는다.

친구 초대/그룹 전용 access gate를 변경한 경우 최소한 다음 targeted test를 먼저 실행한다.

```bash
cd knock-backend
./gradlew :core:core-api:test \
  --tests "com.knock.core.domain.item.ItemServiceTest" \
  --tests "com.knock.core.domain.seller.SellerShareServiceTest" \
  --tests "com.knock.core.api.config.SecurityConfigTest" \
  --tests "com.knock.core.api.controller.v1.ItemControllerTest" \
  --tests "com.knock.core.api.controller.v1.ItemListingControllerTest" \
  --tests "com.knock.core.api.controller.v1.SellerShareControllerTest"
```

### Frontend regression

```bash
make frontend-install
make frontend-test
```

초대 링크 UX 변경 시 `/shop/:token`, `/shop/:token/item/:publicId`, `/login?next=/shop/:token/item/:publicId`, 직접 `/item/:publicId`/`/seller/:memberId` 초대 필요 화면, `?shareToken` 미사용을 함께 확인한다.

### Documentation preview

```bash
make docs-serve
```

브라우저에서 `http://localhost:8088`을 열어 문서를 확인한다.

## 8. Safety rules

- runbook 명령은 로컬 개발/검증 환경을 대상으로 한다.
- 운영 credential, 세션, 토큰, DB 접속 정보는 문서와 로그에 남기지 않는다.
- 외부 배포, 운영 DB 변경, 실서비스 API 호출은 이 runbook의 자동 실행 범위가 아니다.
- 장시간 실행 명령은 별도 터미널에서 실행하고, 중단/재시작 절차를 기록한다.
