package com.knock.core.domain.block.dto;

import com.knock.storage.db.core.block.MemberBlock;

import java.time.LocalDateTime;

public record BlockResult(Long memberId, String nickname, String profileImageUrl, LocalDateTime blockedAt) {

	public static BlockResult from(MemberBlock block) {
		return new BlockResult(block.getBlocked().getId(), block.getBlocked().getNickname(),
				block.getBlocked().getProfileImageUrl(), block.getCreatedAt());
	}

}
