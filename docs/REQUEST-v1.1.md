# Knock Market 구현 상태 가이드 (v1.5, archived)

## 1. 서비스 개요

Knock Market은 판매자가 개인 판매 페이지를 만들고, 카카오톡/인스타그램 등으로 만료 가능한 링크를 공유하는 개인형 중고마켓 서비스입니다.

- 핵심 가치: 개인 판매자의 물건 모음, 공유 가능한 판매 페이지, 지도 기반 픽업 위치
- 핵심 흐름: 회원가입/로그인 -> 상품 등록 -> Naver Map 위치 선택 -> 판매 페이지 공유 -> 예약/승인/완료
- 내부 모델: 상품은 개인 판매자(`member_id`) 기준으로 저장하며 그룹/카테고리/차단/평판 스키마는 사용하지 않는다.

## 2. 아키텍처 요약

- 멀티 모듈 Gradle 프로젝트
- `core:core-api`: Controller/Service/실행 애플리케이션
- `core:core-auth`: 세션 기반 인증
- `storage:db-core`: JPA Entity/Repository
- `storage:memory`: Redis/세션 저장소
- `infra:s3`: 이미지 업로드 인프라
- `tests:api-docs`: REST Docs 공통 테스트 모듈

## 3. 현재 구현 상태 (2026-05-26)

### 구현 완료

- 인증: 이메일/비밀번호 기반 로그인/로그아웃(세션 쿠키)
- 회원: 회원가입, 내 정보 조회/수정, 알림 설정 조회/수정
- 상품: 등록/조회/삭제, 전체 마켓 목록, 판매자별 목록, 내 판매 목록
- 개인 판매 페이지: `/seller/:memberId`, 공유 링크 기반 `/shop/:token`
- 공유 링크: `POST /api/v1/seller-shares`, `GET /api/v1/seller-shares/{token}`
- 거래 위치: Naver Map 검색/지도 선택 기반 위도/경도 저장, 상세 화면 지도/검색 링크 노출
- 찜/예약/알림/후기 기본 기능

### 제거 완료

- 그룹 장터 모델: `Group`, `GroupMember`, 그룹 Repository/Test, `item.group_id` 매핑
- 차단 기능: `member_block` 엔티티/Repository/API/프론트 화면
- 평판 기능: `mannerTemperature`, `ReputationService`, 평판 정책/테스트
- 상품 카테고리: `ItemCategory`, `item.category`, API/프론트 타입 필드

### 고도화 필요

- 공유 링크 폐기/재발급 관리
- Naver Map 장소 검색 UX 고도화
- 소셜 로그인 Kakao 백엔드 연동
- SSE/실시간 알림 푸시
- 운영 환경 인프라 강화

## 4. 참고

- API 상세 스펙: `docs/API_REFERENCE.md`
- 모듈 상세 구조: `docs/MODULE.md`
- 신규 요구사항 기준 문서: `docs/REQUEST-v2.0.md`
