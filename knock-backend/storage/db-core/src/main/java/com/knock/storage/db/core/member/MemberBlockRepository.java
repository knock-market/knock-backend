package com.knock.storage.db.core.member;

import java.util.List;
import java.util.Optional;

public interface MemberBlockRepository {

	MemberBlock save(MemberBlock memberBlock);

	Optional<MemberBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	List<MemberBlock> findByBlockerId(Long blockerId);

	void delete(MemberBlock memberBlock);

}
