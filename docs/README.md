# Knock Documentation

업데이트 기준: 2026-06-15
상태: documentation index

Knock 문서는 역할별 대분류 파일을 기준으로 관리한다. 중복과 drift를 줄이기 위해 active 요구사항, 기술 참조, 개발 관행, 실행 절차는 각각 하나의 canonical 문서만 둔다.

## 문서 지도

| 문서 | 역할 | 먼저 읽을 때 |
|---|---|---|
| `docs/SPEC.md` | 제품 정의, 활성 요구사항, 우선순위, 수용 기준 | 무엇을 만들지/왜 하는지 확인할 때 |
| `docs/REFERENCE.md` | API, 백엔드 모듈, DTO, 테스트 태그, 기술 리스크 | 구현 구조나 API 계약을 확인할 때 |
| `docs/PRACTICES.md` | 코드 컨벤션, Git, AI 협업, 문서 운영 규칙 | 어떻게 개발/리뷰/기록할지 확인할 때 |
| `docs/RUNBOOK.md` | 로컬 하네스, 반복 실행 명령, 안전한 운영 경계 | 어떤 명령으로 실행/검증할지 확인할 때 |

## Canonical rules

- 제품 요구사항과 실행 큐는 `SPEC.md`만 갱신한다.
- API와 모듈 구조는 `REFERENCE.md`만 갱신한다.
- 코드/Git/AI/문서 운영 규칙은 `PRACTICES.md`만 갱신한다.
- 루트 하네스와 로컬 실행 절차는 `RUNBOOK.md`만 갱신한다.
- 완료된 작업과 앞으로 할 작업을 섞지 않는다.

## 변경 전 확인

- 기능 요구사항 변경: `SPEC.md`를 먼저 확인한다.
- API request/response 변경: REST Docs와 `REFERENCE.md`를 함께 확인한다.
- 테스트/하네스 변경: `RUNBOOK.md`와 실제 `Makefile`, `scripts/harness.sh`를 함께 확인한다.
- AI가 요구사항·코드·문서에 기여한 변경: `PRACTICES.md`의 AI 협업 기록 기준을 따른다.
