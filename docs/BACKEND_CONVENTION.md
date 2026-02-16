# Knock Backend Convention (v1)

작성일: 2026-02-14  
대상 프로젝트: `knock-backend`

이 문서는 현재 코드베이스(`core-api`, `core-auth`, `storage/db-core`, `infra/s3`)를 기준으로 정리한 백엔드 개발 컨벤션이다.  
원칙은 "현재 구현과 일치"를 우선하고, 구조적으로 보완이 필요한 항목은 별도 섹션에 명시한다.

## 1. 아키텍처/모듈 컨벤션

- 멀티 모듈 구조를 유지한다.
- API 진입점은 `core:core-api`만 담당한다.
- 인증은 `core:core-auth`, 영속성은 `storage:db-core`, 인프라는 `infra:s3`로 분리한다.
- 공통 Enum은 `core:core-enum`에 둔다.
- 모듈 간 의존은 단방향으로 유지한다.
  - `core-api -> core-auth -> storage`
  - `core-api -> storage`
  - `core-api -> infra`

## 2. 레이어 책임 컨벤션

기본 흐름은 `Controller -> Service -> Repository`이다.

### Controller

- HTTP 입출력과 인증 주체 추출만 담당한다.
- 비즈니스 판단은 Service에 위임한다.
- 응답 타입은 `ApiResponse<T>`를 기본으로 한다.
- 인증 사용자 식별은 `@AuthenticationPrincipal MemberPrincipal`을 사용한다.
- URL은 버전 prefix(`/api/v1`)를 유지한다.

### Service

- 유스케이스 단위 비즈니스 로직을 담당한다.
- 예외는 `CoreException(ErrorType)`으로 던진다.
- 읽기/쓰기 트랜잭션을 명시한다.
  - 조회: `@Transactional(readOnly = true)`
  - 변경: `@Transactional`
- 동시성 제어가 필요한 경우 Service에서 정책을 명확히 선언한다.
  - 예: 예약 승인 시 비관적 락 조회(`findByItemIdForUpdate`)

### Repository

- `Repository interface + RepositoryImpl + JpaRepository` 3단 구조를 유지한다.
- Service는 `Repository interface`에만 의존한다.
- 복잡 조회/락/네이티브 쿼리는 `JpaRepository`에 캡슐화한다.
- 엔티티 저장/조회 외 조합 로직은 `RepositoryImpl`에서 정리한다.

### Entity

- DB 매핑과 최소 도메인 상태 변경 로직을 포함한다.
- 생성은 정적 팩토리(`create`) 또는 Builder를 사용한다.
- 소프트 삭제 엔티티는 `@SQLDelete`, `@SQLRestriction` 패턴을 유지한다.
- 공통 필드(`id`, `createdAt`, `updatedAt`, `deletedAt`)는 `BaseEntity`를 상속한다.

## 3. DTO 컨벤션

레이어별 DTO를 분리한다.

- Controller Request DTO: `core/api/controller/v1/request/*`
- Domain Input DTO(Data): `core/domain/*/dto/*Data`
- Domain Output DTO(Result): `core/domain/*/dto/*Result`
- Controller Response DTO: `core/api/controller/v1/response/*`

권장 변환 흐름:

1. `RequestDto -> Data`
2. `Service(Data) -> Result`
3. `Result -> ResponseDto`
4. `ApiResponse.success(ResponseDto)`

변환 메서드 네이밍:

- `of(...)`, `from(...)`, `toData()` 중 하나를 사용하되 DTO 내부 정적 팩토리를 우선한다.

예시 패턴:

- `MemberSignupRequestDto -> MemberSignupData -> MemberSignupResult -> MemberSignupResponseDto`
- `GroupJoinRequestDto -> GroupJoinData -> groupId -> GroupIdResponseDto`

## 4. 응답/예외 컨벤션

- 공통 응답 포맷은 `ApiResponse`를 사용한다.
  - 성공: `result=SUCCESS`, `data`, `error=null`
  - 실패: `result=ERROR`, `data=null`, `error(code,message,data)`
- 전역 예외 처리는 `ApiControllerAdvice`에서 수행한다.
  - `CoreException`
  - `MethodArgumentNotValidException`
  - `Exception`
- Error 코드는 `ErrorType`/`ErrorCode`에서 관리한다.
- 로그 레벨은 `ErrorType.logLevel`을 따른다.

## 5. 인증/보안 컨벤션

- 세션 기반 인증을 사용한다(`SessionAuthService`).
- 로그인 성공 시 `SecurityContextRepository`에 명시적으로 저장한다.
- 컨트롤러는 `MemberPrincipal`에서 `memberId`를 읽어 사용한다.
- 비밀번호는 `PasswordEncoder(BCrypt)`로 저장/검증한다.
- 민감값(비밀번호, 비밀키, 토큰)은 로그에 출력하지 않는다.

## 6. 트랜잭션/동시성 컨벤션

- 클래스 레벨 기본 readOnly를 줄 수 있는 서비스는 명시한다.
  - 예: `ReservationService`는 클래스 레벨 `@Transactional(readOnly = true)` + 변경 메서드 재정의.
- 예약 등 경쟁 조건이 있는 유스케이스는 DB 락/원자 연산을 활용한다.
  - `PESSIMISTIC_WRITE`
  - 조건부 `INSERT ... WHERE NOT EXISTS ...`
- 비동기 후처리는 `@Async`로 분리하되 예외는 `AsyncExceptionHandler`로 수집한다.

## 7. 테스트 컨벤션

- 테스트 태그 기반 실행 전략을 따른다.
  - `unitTest`: `context`, `restdocs`, `develop` 제외
  - `contextTest`: `@Tag("context")`
  - `restDocsTest`: `@Tag("restdocs")`
  - `developTest`: `@Tag("develop")`
- 테스트 픽스처/상수는 `TestFixtures`, `TestConstants`를 재사용한다.
- 컨트롤러 테스트는 REST Docs 스니펫 생성까지 포함한다.
- 통합 테스트는 주요 사용자 흐름(회원가입 -> 로그인 -> 기능 호출)을 최소 1개 이상 유지한다.

## 8. 문서 운영 컨벤션

- API 변경 시 아래를 함께 갱신한다.
  - `docs/API_REFERENCE.md`
  - `core-api/src/docs/asciidoc/index.adoc` 및 REST Docs 스니펫
  - 필요 시 `docs/API_CHANGE_TODO.md`
- 모듈/구조 변경 시 `docs/MODULE.md`를 함께 갱신한다.

## 9. 현재 코드에서 확인된 예외/개선 권장

아래는 즉시 수정 대상이 아니라, 신규 코드 작성 시 우선적으로 개선할 항목이다.

- 일부 Domain DTO가 Controller DTO를 직접 참조한다.
  - 예: `GroupCreateData.of(GroupCreateRequestDto)`, `ItemCreateData.of(ItemCreateRequestDto)`
  - 권장: Controller에서 Data를 조립하고 Domain DTO는 API 패키지 의존 제거.
- 일부 Domain Service/Response DTO가 저장소 엔티티 또는 API 응답 타입에 직접 의존한다.
  - 예: `ReviewService#getReviewList`가 `ReviewResponse` 반환
  - 권장: Service는 `Result`만 반환하고 Controller에서 `Response`로 변환.
- `LocalDateTime.now()` 사용 로직은 서버 타임존 의존성이 있다.
  - 권장: 비즈니스 시간 계산 지점에 타임존 정책(UTC/KST) 명시.

## 10. 신규 기능 체크리스트

- `request/data/result/response` DTO를 분리했는가?
- Controller가 비즈니스 로직 없이 Service 호출/매핑만 하는가?
- Service에서 `CoreException(ErrorType)`으로 도메인 오류를 표현했는가?
- Repository 인터페이스를 통해 영속성에 접근하는가?
- 읽기/쓰기 트랜잭션 경계를 명시했는가?
- 경쟁 조건 가능성이 있으면 락/원자 연산을 반영했는가?
- 테스트(`unit/context/restdocs`)와 문서를 함께 갱신했는가?
