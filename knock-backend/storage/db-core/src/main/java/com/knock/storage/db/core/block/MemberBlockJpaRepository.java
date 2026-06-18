package com.knock.storage.db.core.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface MemberBlockJpaRepository extends JpaRepository<MemberBlock, Long> {

	@Query(value = "SELECT * FROM member_block WHERE blocker_id = :blockerId AND blocked_id = :blockedId",
			nativeQuery = true)
	Optional<MemberBlock> findByBlockerAndBlockedWithDeleted(@Param("blockerId") Long blockerId,
			@Param("blockedId") Long blockedId);

	@Query("SELECT b FROM MemberBlock b JOIN FETCH b.blocked WHERE b.blocker.id = :blockerId AND b.blocked.id = :blockedId")
	Optional<MemberBlock> findActiveByBlockerAndBlocked(@Param("blockerId") Long blockerId,
			@Param("blockedId") Long blockedId);

	@Query("SELECT b FROM MemberBlock b JOIN FETCH b.blocked WHERE b.blocker.id = :blockerId ORDER BY b.createdAt DESC")
	List<MemberBlock> findAllActiveByBlockerId(@Param("blockerId") Long blockerId);

	@Query("""
			SELECT COUNT(b) > 0 FROM MemberBlock b
			WHERE (b.blocker.id = :firstMemberId AND b.blocked.id = :secondMemberId)
			OR (b.blocker.id = :secondMemberId AND b.blocked.id = :firstMemberId)
			""")
	boolean existsActiveBetween(@Param("firstMemberId") Long firstMemberId,
			@Param("secondMemberId") Long secondMemberId);

}
