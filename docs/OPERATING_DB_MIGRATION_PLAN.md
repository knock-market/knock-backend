# 운영 DB 마이그레이션 계획: 레거시 그룹/차단/평판/카테고리 제거

작성일: 2026-05-29  
범위: 운영 DB 직접 변경이 아니라, 운영 적용 전 위험/선택지/권장 절차/검증/롤백을 문서화한다.

## 1. 배경과 근거

`docs/REQUEST-v2.0.md`는 Knock Market이 그룹형 장터에서 개인 판매자 매대와 공유 링크 중심 서비스로 전환되었고, 운영 DB 마이그레이션 자동화는 당시 비목표였다고 기록한다. 같은 문서는 후속 후보로 기존 로컬/운영 DB의 `item.group_id`, `item.category`, `member.manner_temperature`, `member_block`, 그룹 테이블 제거 스크립트 작성을 남겼다.

현재 `storage:db-core` 코드 기준 운영 스키마는 Hibernate가 자동 변경하지 않는다.

- `knock-backend/storage/db-core/src/main/resources/db-core.yml` 기본/live 계열: `spring.jpa.hibernate.ddl-auto: validate`
- 같은 파일의 `local` 프로필: H2 메모리 DB + `ddl-auto: create`
- `knock-backend/storage/db-core/build.gradle.kts`: JPA와 MySQL/H2 의존성만 있고 Flyway/Liquibase 의존성은 없다.

따라서 로컬 재생성 스키마와 운영 DB의 실제 스키마는 다를 수 있으며, 운영 반영은 별도 절차서 또는 마이그레이션 도구가 필요하다.

## 2. 현재 코드가 기대하는 핵심 스키마

아래는 실제 엔티티/리포지토리 코드에서 확인한 운영 호환성 조건이다.

| 테이블 | 현재 코드에서 필요한 주요 컬럼/제약 | 근거 |
|---|---|---|
| `member` | `email`, `password`, `name`, `nickname`, `profile_image_url`, `provider`, `provider_id`, 알림 설정 boolean 5종, `created_at`, `updated_at`, `deleted_at` | `Member.java`, `BaseEntity.java` |
| `item` | `member_id` NOT NULL, `public_id` NOT NULL UNIQUE length 36, `title`, `description`, `price`, `type`, `status`, `view_count`, 거래 위치 4개 컬럼, soft-delete 타임스탬프 | `Item.java`, `ItemJpaRepository.java` |
| `item_image` | `item_id`, `image_url`, `order_sequence`, soft-delete 타임스탬프 | `ItemImage.java` |
| `bookmark` | `member_id`, `item_id`, `(member_id, item_id)` unique, soft-delete 타임스탬프 | `Bookmark.java` |
| `reservation` | `item_id`, `member_id`, `status`, soft-delete 타임스탬프; 네이티브 INSERT가 `created_at`, `updated_at`를 직접 쓴다 | `Reservation.java`, `ReservationJpaRepository.java` |
| `review` | `reservation_id` UNIQUE, `reviewer_id`, `reviewee_id`, `content`, `score`, soft-delete 타임스탬프 | `Review.java` |
| `notification` | `member_id`, `notification_type`, `content`, `related_url`, `is_read`, soft-delete 타임스탬프 | `Notification.java` |
| `seller_share_link` | `member_id`, `token` UNIQUE, `expires_at`, `active`, `click_count`, `use_count`, base timestamps | `SellerShareLink.java`, `SellerShareLinkJpaRepository.java` |

현재 tracked source 검색 기준, `Group`, `GroupMember`, `MemberBlock`, `ItemCategory`, `mannerTemperature`, `item.group_id`, 상품 도메인의 `category` 참조는 제거되어 있다. 단, `category` 문자열은 Naver 지역 검색 응답 필드에는 남아 있으므로 DB 제거 대상과 혼동하지 않는다.

## 3. 마이그레이션 대상

### 제거 후보

| 대상 | 권장 처리 | 주의점 |
|---|---|---|
| `item.group_id` | 코드 배포 호환성 확인 후 drop | 기존 FK/index가 있으면 FK/index 선제 제거 필요 |
| `item.category` | 코드/API에서 미사용 확인 후 drop | Naver 검색 응답의 `category` 필드와 무관 |
| `member.manner_temperature` | 프로필/평판 코드 미사용 확인 후 drop | 운영 분석/CS에서 사용 중이면 별도 보관 필요 |
| `member_block` | 차단 API/엔티티 미사용 확인 후 drop | 감사/분쟁 이력 보존 필요 여부 확인 |
| 그룹 테이블(`group`, `group_member` 등) | 참조 FK 제거 후 drop 또는 archive | `group`은 예약어/일반명 충돌 가능성이 있어 실제 테이블명 확인 필요 |

### 현재 코드와 운영 DB 차이 확인 대상

운영 DB가 개인 매대 전환 전 스키마라면 제거뿐 아니라 다음 추가/보정도 필요할 수 있다.

- `item.public_id` 추가, 기존 row backfill, unique/not-null 적용
- `item.trade_location_name`, `trade_location_address`, `trade_latitude`, `trade_longitude` 추가
- `seller_share_link` 테이블 추가
- `member.notification_*_enabled` 5개 컬럼 추가 및 기본값/backfill
- `notification.is_read`, `related_url`, `notification_type` 등 현재 엔티티 명과 기존 컬럼명 차이 확인
- soft-delete 컬럼(`created_at`, `updated_at`, `deleted_at`) 존재/nullable/default 확인

## 4. 위험

1. **운영 실제 스키마 미확인 위험**: 문서와 엔티티는 로컬 기준 증거일 뿐이며, 운영 DB의 FK/index/데이터 분포는 아직 확인하지 않았다.
2. **앱 부팅 실패 위험**: live 프로필은 `ddl-auto=validate`라 운영 컬럼이 부족하면 배포 후 애플리케이션 시작 단계에서 실패한다.
3. **데이터 손실 위험**: `DROP COLUMN/TABLE`은 원복이 어렵다. 특히 그룹/차단/매너온도 데이터가 고객지원/분쟁 대응에 쓰였으면 즉시 삭제하면 안 된다.
4. **대용량 DDL 락 위험**: MySQL에서 컬럼/인덱스/FK 변경은 테이블 크기와 버전에 따라 metadata lock 또는 table copy를 유발할 수 있다.
5. **FK 의존성 위험**: `item.group_id`나 `member_block`이 FK/index와 묶여 있으면 단순 drop이 실패하거나 장시간 대기한다.
6. **이중 배포 호환성 위험**: 롤링 배포 중 일부 구버전 인스턴스가 제거된 컬럼/테이블을 참조하면 장애가 난다.
7. **백필 유일성 위험**: 기존 `item` row에 `public_id`를 채울 때 중복/NULL/빈 문자열이 남으면 unique/not-null 제약 추가가 실패한다.
8. **soft-delete unique 위험**: `member.email`, `item.public_id`, `bookmark(member_id,item_id)`, `seller_share_link.token`은 soft-delete row도 unique key를 점유한다. active row만 유일해야 하는지, 전체 이력에서 유일해야 하는지 정책을 먼저 확정해야 한다.
9. **H2/MySQL 차이 위험**: local H2 MySQL mode는 네이티브 INSERT, pessimistic lock, online DDL, FK drop 순서 같은 MySQL 운영 특성을 완전히 재현하지 못한다.
10. **애플리케이션 규칙 미강제 위험**: 거래 위치 all-or-none/range 검증과 판매자별 현재 공유 링크 0~1개 규칙은 주로 서비스 로직에 있다. 기존 row 정리 없이 DB만 변경하면 반쪽 위치값이나 복수 active 링크가 남을 수 있다.
11. **시간/soft-delete 해석 위험**: 엔티티는 `LocalDateTime`과 `NOW()` 기반 컬럼을 사용한다. 운영 DB/session timezone이 정책과 다르면 감사/복구 판단이 흔들릴 수 있다.

## 5. 선택지

### A. Flyway/Liquibase 도입 후 코드화

- 장점: 변경 이력이 재현 가능하고, CI/스테이징/운영 적용 순서가 명확하다.
- 단점: 현재 프로젝트에 도구가 없으므로 초기 도입/운영 권한/기존 DB baseline 결정이 필요하다.
- 위험: 운영 DB 상태를 먼저 baseline으로 고정하지 않으면 첫 migration부터 실패할 수 있다.

### B. SQL 운영 절차서 우선 작성

- 장점: 지금 필요한 위험과 운영 승인 절차를 빠르게 드러낼 수 있다.
- 단점: 수동 실행 실수와 환경별 drift 방지가 약하다.
- 위험: 한 번성 SQL만 남으면 이후 스키마 변경 관리가 계속 누락될 수 있다.

### C. 하이브리드: SQL 절차서로 1회 안전 적용 후 migration tool baseline

- 장점: 운영 실사를 먼저 끝낸 뒤, 확인된 최종 스키마를 baseline으로 삼아 이후 변경은 코드화할 수 있다.
- 단점: 1회 수동 절차와 도구 도입을 모두 관리해야 한다.
- 위험: baseline 시점/커밋/운영 적용 시각을 엄격히 기록해야 한다.

## 6. 권장안

**권장: C에 가까운 단계적 접근.** 이번 작업은 SQL 운영 절차서를 먼저 완성하고, 실제 운영 적용 전 스테이징에서 동일 절차를 검증한다. 운영 반영 후에는 확인된 스키마를 baseline으로 삼아 Flyway/Liquibase 도입 여부를 결정한다.

핵심 원칙은 **expand → backfill/verify → deploy compatible app → contract** 이다.

1. 제거보다 추가/보정이 먼저다. 현재 코드가 필요한 컬럼/테이블이 운영에 모두 있는지 확인하고 부족분을 추가한다.
2. `public_id`처럼 NOT NULL/UNIQUE가 필요한 값은 nullable 컬럼 추가 → backfill → 검증 → 제약 적용 순서로 진행한다.
3. 레거시 컬럼/테이블 drop은 새 코드가 운영에서 안정화되고, 백업/아카이브가 끝난 뒤 마지막 단계에서 수행한다.
4. drop 전 최소 1회 롤링 배포 기간 동안 구버전 코드가 완전히 제거되었는지 확인한다.

## 7. 운영 적용 절차 초안

### 7.1 사전 준비

- 운영 DB 엔진/버전, 테이블 row count, 테이블 size, FK/index 목록을 확인한다.
- 운영과 동일한 버전의 스테이징/복제본에서 전체 절차를 먼저 실행한다.
- 배포 대상 애플리케이션 커밋, 문서 버전, 실행자, 승인자, 예정 시각을 기록한다.
- 전체 백업 또는 point-in-time recovery 가능 시점을 확보한다.
- 애플리케이션 롤백 이미지/커밋과 DB rollback SQL 위치를 같이 준비한다.
- 비밀값은 절차서, 로그, PR, 채팅에 남기지 않는다.

### 7.2 운영 스키마 실사 쿼리

아래 쿼리는 결과 확인용이다. 실제 DB명은 운영 콘솔에서 선택된 schema를 사용하고, 결과는 민감값 없이 컬럼/제약 메타데이터만 공유한다.

```sql
-- 테이블 존재 확인
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'member', 'item', 'item_image', 'bookmark', 'reservation', 'review',
    'notification', 'seller_share_link', 'member_block', 'group', 'group_member'
  )
ORDER BY table_name;

-- 제거/보정 대상 컬럼 존재 확인
SELECT table_name, column_name, column_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
    (table_name = 'item' AND column_name IN (
      'group_id', 'category', 'public_id', 'trade_location_name',
      'trade_location_address', 'trade_latitude', 'trade_longitude', 'view_count'
    ))
    OR (table_name = 'member' AND column_name IN (
      'manner_temperature', 'notification_push_enabled',
      'notification_new_items_enabled', 'notification_chat_enabled',
      'notification_marketing_enabled', 'notification_sound_enabled'
    ))
    OR (table_name IN ('member_block', 'group', 'group_member'))
  )
ORDER BY table_name, column_name;

-- FK/index 의존성 확인
SELECT table_name, constraint_name, column_name, referenced_table_name, referenced_column_name
FROM information_schema.key_column_usage
WHERE table_schema = DATABASE()
  AND (table_name IN ('item', 'member_block', 'group', 'group_member')
       OR referenced_table_name IN ('group', 'group_member', 'member_block'))
ORDER BY table_name, constraint_name, ordinal_position;
```

### 7.3 Expand: 현재 코드 필요 스키마 보정

운영 실사 결과 누락된 항목만 적용한다. 아래 SQL은 템플릿이며, 운영 실행 전 DB 버전별 online DDL 지원 여부와 FK/index 이름을 확인해 확정한다.

운영 SQL은 MySQL에서 dry-run 해야 한다. H2 local profile은 빠른 엔티티 검증용으로만 사용하고, 네이티브 쿼리/락/DDL은 운영과 같은 MySQL 버전에서 확인한다.

```sql
-- 예: item.public_id가 없다면 nullable로 먼저 추가한다.
ALTER TABLE item ADD COLUMN public_id VARCHAR(36) NULL;

-- 기존 row backfill. MySQL UUID()는 row별 호출 결과가 달라야 한다.
UPDATE item
SET public_id = UUID()
WHERE public_id IS NULL OR public_id = '';

-- backfill 검증 후 제약 적용.
SELECT public_id, COUNT(*) AS cnt
FROM item
GROUP BY public_id
HAVING cnt > 1;

SELECT COUNT(*) AS missing_public_id
FROM item
WHERE public_id IS NULL OR public_id = '';

ALTER TABLE item MODIFY public_id VARCHAR(36) NOT NULL;
ALTER TABLE item ADD UNIQUE KEY uk_item_public_id (public_id);

-- 거래 위치 컬럼이 없다면 추가한다.
ALTER TABLE item ADD COLUMN trade_location_name VARCHAR(255) NULL;
ALTER TABLE item ADD COLUMN trade_location_address VARCHAR(255) NULL;
ALTER TABLE item ADD COLUMN trade_latitude DOUBLE NULL;
ALTER TABLE item ADD COLUMN trade_longitude DOUBLE NULL;

-- view_count가 nullable이거나 없으면 현재 엔티티 기본값에 맞춘다.
ALTER TABLE item ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;
UPDATE item SET view_count = 0 WHERE view_count IS NULL;

-- 알림 설정 컬럼이 없다면 기본값과 함께 추가한다.
ALTER TABLE member ADD COLUMN notification_push_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE member ADD COLUMN notification_new_items_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE member ADD COLUMN notification_chat_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE member ADD COLUMN notification_marketing_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE member ADD COLUMN notification_sound_enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- 거래 위치는 현재 엔티티상 nullable이지만 서비스는 all-or-none/range를 요구한다.
-- 운영 데이터에 반쪽 좌표가 있으면 앱 배포 전 정리 또는 예외 허용 정책을 결정한다.
SELECT COUNT(*) AS partial_trade_location_rows
FROM item
WHERE (trade_latitude IS NULL) <> (trade_longitude IS NULL)
   OR (trade_latitude IS NULL AND (trade_location_name IS NOT NULL OR trade_location_address IS NOT NULL))
   OR (trade_latitude IS NOT NULL AND (trade_latitude < -90 OR trade_latitude > 90
       OR trade_longitude < -180 OR trade_longitude > 180));

-- seller_share_link가 없다면 생성한다. FK/index 이름은 운영 naming convention에 맞춘다.
CREATE TABLE seller_share_link (
  id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  token VARCHAR(255) NOT NULL,
  expires_at DATETIME(6) NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  click_count BIGINT NOT NULL DEFAULT 0,
  use_count BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  deleted_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seller_share_link_token (token),
  KEY idx_seller_share_link_member_id (member_id),
  CONSTRAINT fk_seller_share_link_member
    FOREIGN KEY (member_id) REFERENCES member (id)
);

-- 판매자별 현재 링크는 서비스가 최신 1개를 사용하고 이전 링크를 비활성화한다.
-- 기존 row가 있다면 member별 최신 1개만 active=true로 정리한다.
UPDATE seller_share_link s
JOIN (
  SELECT member_id, MAX(id) AS latest_id
  FROM seller_share_link
  WHERE deleted_at IS NULL
  GROUP BY member_id
) latest ON latest.member_id = s.member_id
SET s.active = CASE WHEN s.id = latest.latest_id THEN TRUE ELSE FALSE END;

SELECT member_id, COUNT(*) AS active_count
FROM seller_share_link
WHERE active = TRUE AND deleted_at IS NULL
GROUP BY member_id
HAVING active_count > 1;
```

### 7.4 Compatible deploy

- Expand 적용 후 현재 애플리케이션을 스테이징에서 `validate` 모드로 기동한다.
- 스테이징 smoke test가 통과하면 운영에 애플리케이션을 먼저 배포한다.
- 롤링 배포 중 구버전 인스턴스가 완전히 종료되었는지 확인한다.
- 운영 로그에서 Hibernate validation 오류, SQL unknown column/table 오류, FK 오류가 없는지 확인한다.

### 7.5 Contract: 레거시 스키마 제거

레거시 데이터는 drop 전에 아카이브하거나 백업으로 복구 가능한 상태를 확인한다. 테이블/컬럼/FK/index 이름은 실사 결과에 맞춰 확정한다.

```sql
-- 예: item.group_id FK/index 제거 후 컬럼 제거
-- ALTER TABLE item DROP FOREIGN KEY <fk_item_group_id>;
-- DROP INDEX <idx_item_group_id> ON item;
ALTER TABLE item DROP COLUMN group_id;

-- 예: 상품 카테고리 컬럼 제거
ALTER TABLE item DROP COLUMN category;

-- 예: 매너온도 컬럼 제거
ALTER TABLE member DROP COLUMN manner_temperature;

-- 예: 차단/그룹 테이블 제거. FK가 있으면 FK부터 제거한다.
DROP TABLE member_block;
DROP TABLE group_member;
DROP TABLE `group`;
```

대용량 테이블에서는 위 DDL을 그대로 운영 피크 시간에 실행하지 않는다. MySQL 버전별 `ALGORITHM=INPLACE/INSTANT`, `LOCK=NONE` 지원 여부를 사전 검증하거나, gh-ost/pt-online-schema-change 같은 온라인 스키마 변경 도구 사용을 검토한다.

## 8. 검증 체크리스트

### DB 메타데이터 검증

- `seller_share_link` 존재 및 `token` unique 확인
- `item.public_id` not null/unique, NULL/빈 문자열/중복 0건 확인
- soft-delete row까지 포함한 unique 정책이 의도와 맞는지 확인하고, deleted row collision이 있으면 보존/정리 방식을 승인받는다.
- 거래 위치 row가 all-or-none/range 규칙을 만족하는지 확인한다.
- 판매자별 active 공유 링크가 0~1개인지 확인한다.
- `item.group_id`, `item.category`, `member.manner_temperature` 제거 확인
- `member_block`, 그룹 테이블 제거 또는 archive 완료 확인
- FK/index에 제거 대상 참조가 남지 않았는지 확인

### 애플리케이션 검증

- live와 동일한 profile에서 애플리케이션이 Hibernate validate를 통과해 부팅되는지 확인
- 상품 등록/목록/상세 공개 조회가 동작하는지 확인
- 기존 상품 row가 `/item/{publicId}` 공개 상세로 조회되는지 확인
- 공유 링크 생성/조회/중단과 `click_count`, `use_count` 업데이트가 동작하는지 확인
- MySQL 환경에서 동시 예약 생성, `FOR UPDATE` 조회, 공유 링크 클릭/사용 카운터 smoke test를 수행한다.
- 예약 생성/승인/완료 및 알림 생성/읽음 처리 동작 확인
- 북마크/후기 목록 조회에서 soft-delete 필터와 join 조회가 정상인지 확인

### 권장 명령

로컬/CI에서 문서 변경과 무관하게 현재 스키마 코드가 깨지지 않았는지 확인한다.

```bash
cd knock-backend
./gradlew :storage:db-core:test
./gradlew :storage:db-core:compileJava
```

전체 회귀가 필요하면 루트 하네스 기준으로 다음을 추가한다.

```bash
make verify
```

## 9. 롤백/안전 절차

### Expand 단계 롤백

- nullable 컬럼 추가나 새 테이블 추가는 앱 구버전과 대체로 호환된다. 문제가 생기면 앱을 이전 버전으로 롤백하고, 추가 컬럼/테이블은 즉시 drop하지 않는다.
- `public_id` backfill 오류가 발견되면 unique/not-null 적용 전에는 값을 재생성할 수 있다.
- unique/not-null 적용 후 문제가 발견되면 앱 롤백 후 제약 완화 SQL을 별도 승인받아 실행한다.

### Contract 단계 롤백

- drop 이후에는 DB 자체 rollback이 어렵다. 반드시 drop 전 백업/PITR 또는 archive table을 확보한다.
- 운영 장애가 제거 컬럼/테이블 참조로 발생하면 우선 애플리케이션을 호환 버전으로 재배포한다.
- drop 대상 데이터가 고객지원/감사 용도일 가능성이 있으면 별도 archive table 또는 외부 백업에서 복원 가능한지 확인하기 전에는 contract 단계를 진행하지 않는다.
- 데이터 복구가 필요하면 백업에서 제거 컬럼/테이블만 복원하는 절차를 사용한다. 이때 현재 운영 row와의 PK/FK 충돌 가능성을 별도 점검한다.

### 중단 기준

다음 중 하나라도 발생하면 즉시 DDL 진행을 멈추고 앱 롤백 또는 읽기 전용 대응을 검토한다.

- metadata lock 대기 증가 또는 쓰기 지연이 서비스 SLO를 넘는다.
- Hibernate validate 실패 또는 `Unknown column/table` 오류가 반복된다.
- `public_id` 중복/누락이 1건 이상 남아 있다.
- FK 제거/drop table 단계에서 예상하지 못한 참조가 발견된다.

## 10. 완료 기준

- 운영과 동일한 스테이징/복제본에서 expand/contract SQL이 재현 가능하게 통과한다.
- 현재 앱이 `ddl-auto=validate`로 부팅되고 핵심 사용자 흐름 smoke test가 통과한다.
- 레거시 제거 대상 컬럼/테이블이 운영에서 제거되었거나, 보관 필요 사유와 제거 예정일이 문서화되어 있다.
- 백업/롤백 시점, 실행자, 승인자, 적용 SQL, 검증 결과가 배포 기록에 남아 있다.
- 후속 작업으로 Flyway/Liquibase baseline 여부가 결정되어 `docs/NEXT_STEPS.md`에 반영된다.
