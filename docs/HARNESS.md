# Knock Harness Engineering

이 문서는 루트에서 `knock-backend`, `knock-frontend`, `docs`를 하나의 작업 단위로 다루기 위한 하네스 레이어를 설명합니다.

## 목표

- 루트에서 백엔드, 프론트엔드, 문서 작업의 공통 진입점을 제공한다.
- 로컬 의존성, 테스트, 실행 명령을 프로젝트 내부에 명시한다.
- `knock-frontend`가 같은 루트에 있거나 형제 디렉터리에 있어도 동일한 명령으로 동작하게 한다.
- 새 하네스 작업을 추가할 때 `harness/harness.yml`, `scripts/harness.sh`, `Makefile`만 보면 되게 한다.

## 빠른 시작

```bash
make doctor
make deps-up
make backend-run
```

백엔드만 검증하려면 다음 명령을 사용합니다.

```bash
make backend-unit
make backend-test
```

기본 품질 게이트를 실행하려면 다음 명령을 사용합니다.

```bash
make verify
```

`knock-frontend`가 없으면 프론트엔드 테스트 단계는 성공으로 건너뜁니다. 현재 워크트리처럼 백엔드와 문서만 받은 환경에서도 하네스 검증을 진행할 수 있게 하기 위함입니다.

## 명령 목록

| 명령 | 설명 |
| --- | --- |
| `make doctor` | 필수 도구와 각 축의 경로를 확인합니다. |
| `make deps-up` | `knock-backend/docker/local/docker-compose.yml`로 로컬 의존성을 실행합니다. |
| `make deps-down` | 로컬 의존성을 종료합니다. |
| `make backend-run` | `:core:core-api:bootRun`으로 API 서버를 실행합니다. |
| `make backend-test` | 백엔드 전체 테스트를 실행합니다. |
| `make backend-unit` | `unitTest` 태스크를 실행합니다. |
| `make backend-context` | `contextTest` 태스크를 실행합니다. |
| `make docs-serve` | 문서를 `http://localhost:8088`에서 제공합니다. |
| `make frontend-install` | 프론트엔드 의존성을 설치합니다. |
| `make frontend-run` | 프론트엔드 개발 서버를 실행합니다. |
| `make frontend-test` | 프론트엔드 테스트를 실행합니다. |
| `make verify` | `doctor`, 백엔드 단위 테스트, 프론트엔드 테스트를 순서대로 실행합니다. |

## 파일 역할

- `harness/harness.yml`: 하네스 매니페스트입니다. 축, 경로, 명령, 헬스 체크 URL, 로컬 의존성을 선언합니다.
- `scripts/harness.sh`: 사람이 실행하는 실제 명령 라우터입니다.
- `Makefile`: 짧은 별칭을 제공합니다.

## 확장 규칙

1. 새 축을 추가할 때는 먼저 `harness/harness.yml`에 경로와 명령을 선언합니다.
2. 사람이 자주 실행할 명령만 `scripts/harness.sh`와 `Makefile`에 연결합니다.
3. CI에서도 같은 명령을 재사용합니다. 예를 들어 백엔드 단위 테스트는 `make backend-unit`, 기본 품질 게이트는 `make verify`를 사용합니다.
4. 외부 서비스가 필요한 검증은 `deps:up`에 붙이거나 별도 명령으로 분리합니다.

## 현재 구조 메모

현재 확인된 워크트리에는 `docs`와 `knock-backend`가 있고, `knock-frontend`는 존재하지 않습니다. 하네스는 다음 순서로 프론트엔드를 탐색합니다.

1. 루트 내부 `knock-frontend`
2. 루트의 형제 디렉터리 `../knock-frontend`

둘 다 없으면 프론트엔드 관련 명령은 안내 메시지를 출력하고 종료합니다.
