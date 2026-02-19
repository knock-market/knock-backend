package com.knock.storage.db.core.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberBlockRepositoryImpl implements MemberBlockRepository {

	private final MemberBlockJpaRepository memberBlockJpaRepository;

	@Override
	public MemberBlock save(MemberBlock memberBlock) {
		return memberBlockJpaRepository.save(memberBlock);
	}

	@Override
	public Optional<MemberBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
		return memberBlockJpaRepository.findByBlockerIdAndBlockedId(blockerId, blockedId);
	}

	@Override
	public boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
		return memberBlockJpaRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
	}

	@Override
	public List<MemberBlock> findByBlockerId(Long blockerId) {
		return memberBlockJpaRepository.findByBlockerIdOrderByCreatedAtDesc(blockerId);
	}

	@Override
	public void delete(MemberBlock memberBlock) {
		memberBlockJpaRepository.delete(memberBlock);
	}

}
