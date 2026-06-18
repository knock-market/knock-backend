package com.knock.storage.db.core.block;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberBlockRepositoryImpl implements MemberBlockRepository {

	private final MemberBlockJpaRepository jpaRepository;

	@Override
	public MemberBlock save(MemberBlock block) {
		return jpaRepository.save(block);
	}

	@Override
	public Optional<MemberBlock> findByBlockerAndBlockedWithDeleted(Long blockerId, Long blockedId) {
		return jpaRepository.findByBlockerAndBlockedWithDeleted(blockerId, blockedId);
	}

	@Override
	public Optional<MemberBlock> findActiveByBlockerAndBlocked(Long blockerId, Long blockedId) {
		return jpaRepository.findActiveByBlockerAndBlocked(blockerId, blockedId);
	}

	@Override
	public List<MemberBlock> findAllActiveByBlockerId(Long blockerId) {
		return jpaRepository.findAllActiveByBlockerId(blockerId);
	}

	@Override
	public boolean existsActiveBetween(Long firstMemberId, Long secondMemberId) {
		return jpaRepository.existsActiveBetween(firstMemberId, secondMemberId);
	}

	@Override
	public void delete(MemberBlock block) {
		jpaRepository.delete(block);
	}

}
