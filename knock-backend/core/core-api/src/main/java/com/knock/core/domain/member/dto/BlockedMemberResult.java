package com.knock.core.domain.member.dto;

import com.knock.storage.db.core.member.MemberBlock;

import java.time.LocalDateTime;

public record BlockedMemberResult(Long id, String name, LocalDateTime blockedAt) {

	public static BlockedMemberResult from(MemberBlock memberBlock) {
		return new BlockedMemberResult(memberBlock.getBlocked().getId(), memberBlock.getBlocked().getName(),
				memberBlock.getCreatedAt());
	}

}
