# Knock Engineering Practices

업데이트 기준: 2026-06-15
상태: engineering practices and collaboration source of truth

이 문서는 Knock의 코드 컨벤션, Git 컨벤션, AI 협업 기록, 문서 작성 규칙을 통합한 기준이다. 제품 요구사항은 `docs/SPEC.md`, API/모듈 참조는 `docs/REFERENCE.md`, 실행 절차는 `docs/RUNBOOK.md`를 따른다.

## 1. 기본 개발 원칙

- 코드는 `docs/SPEC.md`와 API 문서의 요구사항을 구현한다.
- 새로운 정책을 추가하거나 변경하면 구현 전후로 관련 문서를 갱신한다.
- 과한 추상화보다 읽히는 유스케이스 흐름을 우선한다.
- 입력 검증, 권한 확인, 공개 필드 제한은 HTTP/API 경계에서 먼저 드러나야 한다.
- 시간 정책은 암묵적 기본 시간대에 의존하지 않는다. 서버 계산은 명시적 `Clock` 또는 문서화된 기준 시간대를 사용한다.
- 외부 서비스 키와 민감값은 백엔드 환경변수에만 둔다. 프론트에는 공개 가능한 키만 둔다.

## 2. 코드 스타일

### Backend

- URL은 `/api/v1` prefix를 유지한다.
- Controller는 request validation, 인증 주체 추출, DTO 변환만 담당한다.
- Service는 HTTP DTO나 JPA entity에 직접 의존하지 않는다.
- 조회는 `@Transactional(readOnly = true)`, 변경은 `@Transactional`을 명시한다.
- 경쟁 조건 가능성이 있으면 DB 락, 유니크 제약, 조건부 쓰기, 멱등 처리 중 하나를 명시적으로 선택하고 테스트한다.
- 도메인 오류는 `CoreException(ErrorType)` 또는 해당 모듈의 구체 예외로 표현한다.
- Service는 repository 인터페이스에 의존한다.
- soft delete와 상태 전이는 삭제 전에 명시적으로 처리한다.

### DTO와 이름

- Request DTO는 HTTP 입력 형식 검증과 정규화만 담당한다.
- Domain input은 `*Data` 또는 `*Command`, output은 `*Result` 성격을 가진 이름을 쓴다.
- Response DTO는 entity를 바로 노출하지 않는다.
- 필드명은 API JSON 필드명과 최대한 일치시킨다.
- 공개 URL에는 `publicId` 같은 공개 식별자를 사용하고, 내부 PK 노출 여부는 API 문서에 명시한다.

### Frontend

- 페이지 컴포넌트는 `pages/`, 재사용 UI는 `components/`, API 경계는 `services/`, 순수 유틸은 `utils/`에 둔다.
- 서버 응답 타입은 `types.ts` 또는 기능별 타입으로 명시하고 임의 `any`를 피한다.
- 보호 라우트와 공개 라우트 정책은 `App.tsx`/인증 유틸과 API 문서가 함께 맞아야 한다.
- 외부 SDK 초기화는 전용 컴포넌트/유틸에 둔다.
- 사용자 입력은 화면에서 1차 검증하고, 서버 오류 메시지는 안전하게 정규화해 표시한다.

## 3. API 응답과 예외

- v1 JSON API 성공 응답은 `ApiResponse<T>` 규칙을 따른다.
- `/health` 상태 점검과 OAuth 302 redirect 엔드포인트는 `ResponseEntity` 예외를 허용하되 API 문서에 응답 형태를 명시한다.
- 실패 응답은 코드와 사람이 읽을 수 있는 메시지를 포함한다.
- validation 실패, 인증 실패, 권한 실패, 도메인 충돌은 서로 다른 에러 코드로 구분한다.
- 서버 내부 예외 메시지, stack trace, 비밀값, 외부 API credential은 응답이나 로그에 노출하지 않는다.

## 4. 테스트 스타일

- 테스트 이름은 깨지는 정책을 문장으로 설명한다.
- 성공 경로 1개와 실패/경계 경로 1개 이상을 우선 확보한다.
- 시간 의존 테스트는 고정 Clock 또는 명시적 fixture 시각을 사용한다.
- 동시성/멱등/락 정책은 mock만으로 끝내지 말고 DB 제약이나 통합 테스트로 보강한다.
- REST Docs 테스트는 API 문서 갱신 책임을 함께 가진다.

예시:

```text
예약 생성은 itemId가 없으면 400을 반환한다
같은 상품을 중복 북마크하면 토글 상태만 바뀐다
판매자 본인은 자신의 상품 조회수에 집계되지 않는다
만료된 공유 링크는 공개 매대를 반환하지 않는다
```

## 5. 문서 운영 규칙

### 문서 지도

| 문서 | 역할 | 갱신 시점 |
|---|---|---|
| `docs/SPEC.md` | 제품 목표, 요구사항, 로드맵 | 제품 방향·수용 기준 변경 |
| `docs/REFERENCE.md` | API, 모듈, 백엔드 기술 참조 | Controller/DTO/API/모듈 변경 |
| `docs/PRACTICES.md` | 코드/Git/AI/문서 운영 규칙 | 개발·협업 정책 변경 |
| `docs/RUNBOOK.md` | 루트 하네스와 반복 실행 절차 | Makefile/scripts/harness 변경 |
| `docs/README.md` | 얇은 문서 인덱스 | 문서 구조 변경 |

### 변경 유형별 갱신 기준

| 변경 유형 | 먼저 갱신/검토 | 함께 확인 |
|---|---|---|
| 제품 요구사항 | `SPEC.md` | `REFERENCE.md`, QA 기준 |
| API request/response | REST Docs, `REFERENCE.md` | Controller 테스트, 프론트 호출부 |
| 백엔드 레이어/모듈 | `REFERENCE.md` | Gradle 의존성, 테스트 태그 |
| 프론트 화면/흐름 | `SPEC.md` 또는 기능 메모 | 공개/인증 라우트 QA |
| 반복 실행 절차 | `RUNBOOK.md` | Makefile, scripts |
| 공통 개발 규칙 | `PRACTICES.md` | 기존 예외/적용 범위 |
| AI 보조 작업 | `PRACTICES.md` | 실제 검증 명령, 사람이 확정한 결정 |

### 문서 작성 원칙

- 스펙과 코드 이름을 맞춘다. 예: `publicId`, `seller-shares`, `tradeLatitude`.
- 정책, API, 데이터 모델, 테스트 기준을 한 변경 안에서 함께 갱신한다.
- 완료된 작업과 앞으로 할 작업을 섞지 않는다.
- 수치, 날짜, 명령 출력은 재현 가능한 근거를 함께 남긴다.
- 외부 서비스 키, 세션, 토큰, 운영 DB 접속 정보는 예시로도 기록하지 않는다.

## 6. Git convention

커밋 메시지는 변경 목적과 검증 책임이 먼저 보이게 작성한다. 첫 줄은 “무엇을 바꿨는가”보다 “왜 필요한가”를 설명한다.

```text
<intent line: 왜 이 변경이 필요한지>

<optional body: 제약, 접근 방식, 결정 이유>

Constraint: <변경을 제한한 외부 조건>
Rejected: <고려했지만 버린 대안> | <사유>
Confidence: <low|medium|high>
Scope-risk: <narrow|moderate|broad>
Directive: <후속 수정자가 지켜야 할 주의점>
Tested: <실제로 검증한 명령/범위>
Not-tested: <검증하지 못한 항목>
Refs: #<issue-number>
AI-Model: <사용한 AI 모델명>
AI-Policy: <AI 사용 범위와 최종 검증 책임>
```

권장 타입: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.

브랜치 형식:

```text
<type>/issue-<issue-number>-<short-description>
<type>/<short-description>
```

규칙:

- 하나의 커밋은 하나의 의도를 가진다.
- 문서 변경은 가능하면 코드 변경과 분리한다.
- 설명은 소문자 kebab-case를 사용한다.
- 실패한 실험은 커밋하지 말고, 의미 있는 결정은 PR/문서에 `Rejected:`로 남긴다.

## 7. PR convention

PR 본문은 리뷰어가 빠르게 맥락과 검증 상태를 파악할 수 있게 작성한다. 상세 배경은 이슈나 문서에 두고, PR에는 결정에 필요한 핵심만 남긴다.

필수 구조:

```md
## 요약

## 핵심 수정사항

## AS-IS

## TO-BE

## 영향 범위

## 검증

## 리스크 / 롤백

## 관련 문서 / 이슈

## AI 사용 기록
```

작성 규칙:

- `요약`은 2~4문장으로 문제와 기대 효과를 설명한다.
- `핵심 수정사항`은 리뷰 우선순위가 높은 변경만 bullet로 적는다.
- `AS-IS`와 `TO-BE`는 변경 전후 차이를 사용자/API/운영 관점에서 짧게 대비한다.
- `검증`은 실제 실행한 명령과 미실행 항목을 분리한다.
- `리스크 / 롤백`에는 의도적으로 남긴 위험과 되돌릴 때 함께 봐야 할 범위를 적는다.
- AI가 관여했다면 `AI 사용 기록`을 남기고 사람이 확인한 검증 근거를 함께 적는다.

## 8. AI collaboration 기록

아래 중 하나라도 해당하면 PR 설명, 이슈, 커밋 trailer, 또는 별도 변경 메모에 기록한다.

- AI가 제품 요구사항이나 수용 기준을 정리했다.
- AI가 코드, 테스트, 운영 절차, 문서 초안을 작성했다.
- AI가 성능/보안/동시성/아키텍처 판단에 영향을 줬다.
- AI가 문서 초안을 만들거나 기존 문서를 재구성했다.

권장 기록 형식:

```text
AI-Model: <모델/도구>
AI-Policy: <AI가 수행한 범위와 사람이 검증한 범위>
AI-Used-For:
- <요구사항 정리/테스트 초안/문서 초안 등>
Human-Verified:
- <실행한 테스트, 확인한 문서, 수동 QA>
Rejected:
- <채택하지 않은 AI 제안과 이유>
```

AI 산출물 검증 기준:

- 제품 요구사항: `docs/SPEC.md`와 충돌하지 않는가?
- API: `docs/REFERENCE.md`, REST Docs, Controller/DTO 테스트가 일치하는가?
- 보안: 비밀값, 세션, 토큰, 외부 API credential이 노출되지 않았는가?
- 데이터: 공개 API가 의도된 공개 필드만 반환하는가?
- 시간: 현재 시각과 시간대 정책이 명시되어 있는가?
- 동시성: 멱등, 락, unique 제약, retry 정책이 테스트로 검증되는가?
- 문서: 변경된 정책과 실행 명령이 최신 상태인가?

## 9. 신규 작업 체크리스트

- 구현하려는 정책이 제품/API 문서에 정의되어 있는가?
- Controller가 Service 호출과 매핑만 담당하는가?
- Request, Data/Command, Result, Response가 섞이지 않았는가?
- 공개 API가 의도된 공개 필드만 반환하는가?
- 시간, 위치, 금액, 좌표 같은 경계값 검증이 있는가?
- 경쟁 조건 가능성에 대한 락/제약/멱등 전략을 선택했는가?
- 성공/실패/경계 테스트와 문서를 함께 갱신했는가?
- 비밀값과 민감값을 코드·로그·문서에 남기지 않았는가?
