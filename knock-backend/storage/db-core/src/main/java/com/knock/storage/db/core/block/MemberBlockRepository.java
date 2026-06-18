package com.knock.storage.db.core.block;

import java.util.List;
import java.util.Optional;

public interface MemberBlockRepository {

	MemberBlock save(MemberBlock block);

	Optional<MemberBlock> findByBlockerAndBlockedWithDeleted(Long blockerId, Long blockedId);

	Optional<MemberBlock> findActiveByBlockerAndBlocked(Long blockerId, Long blockedId);

	List<MemberBlock> findAllActiveByBlockerId(Long blockerId);

	boolean existsActiveBetween(Long firstMemberId, Long secondMemberId);

	void delete(MemberBlock block);

}
