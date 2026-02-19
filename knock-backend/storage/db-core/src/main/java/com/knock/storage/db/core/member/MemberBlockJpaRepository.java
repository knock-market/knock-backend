package com.knock.storage.db.core.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface MemberBlockJpaRepository extends JpaRepository<MemberBlock, Long> {

	Optional<MemberBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	List<MemberBlock> findByBlockerIdOrderByCreatedAtDesc(Long blockerId);

}
