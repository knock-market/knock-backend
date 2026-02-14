package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.member.dto.BlockedMemberResult;

import java.time.LocalDateTime;

public record BlockedMemberResponseDto(Long id, String name, LocalDateTime blockedAt) {

	public static BlockedMemberResponseDto from(BlockedMemberResult result) {
		return new BlockedMemberResponseDto(result.id(), result.name(), result.blockedAt());
	}

}
