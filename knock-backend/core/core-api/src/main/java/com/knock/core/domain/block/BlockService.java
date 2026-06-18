package com.knock.core.domain.block;

import com.knock.core.domain.block.dto.BlockResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.block.MemberBlock;
import com.knock.storage.db.core.block.MemberBlockRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

	private final MemberBlockRepository memberBlockRepository;

	private final MemberRepository memberRepository;

	@Transactional
	public BlockResult blockMember(Long blockerId, Long blockedId) {
		validateDifferentMembers(blockerId, blockedId);
		Member blocker = memberRepository.findById(blockerId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		Member blocked = memberRepository.findById(blockedId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		MemberBlock block = memberBlockRepository.findByBlockerAndBlockedWithDeleted(blockerId, blockedId)
			.map(existing -> {
				if (existing.getDeletedAt() != null) {
					existing.restore();
				}
				return existing;
			})
			.orElseGet(() -> memberBlockRepository.save(MemberBlock.create(blocker, blocked)));

		return BlockResult.from(block);
	}

	@Transactional
	public void unblockMember(Long blockerId, Long blockedId) {
		validateDifferentMembers(blockerId, blockedId);
		memberBlockRepository.findActiveByBlockerAndBlocked(blockerId, blockedId)
			.ifPresent(memberBlockRepository::delete);
	}

	public List<BlockResult> getMyBlocks(Long blockerId) {
		return memberBlockRepository.findAllActiveByBlockerId(blockerId).stream().map(BlockResult::from).toList();
	}

	public boolean isBlockedBetween(Long firstMemberId, Long secondMemberId) {
		if (firstMemberId.equals(secondMemberId)) {
			return false;
		}
		return memberBlockRepository.existsActiveBetween(firstMemberId, secondMemberId);
	}

	public void validateInteractionAllowed(Long firstMemberId, Long secondMemberId) {
		if (isBlockedBetween(firstMemberId, secondMemberId)) {
			throw new CoreException(ErrorType.BLOCKED_INTERACTION);
		}
	}

	private void validateDifferentMembers(Long blockerId, Long blockedId) {
		if (blockerId.equals(blockedId)) {
			throw new CoreException(ErrorType.SELF_BLOCK_NOT_ALLOWED);
		}
	}

}
