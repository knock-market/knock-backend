package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.block.dto.BlockResult;

import java.time.LocalDateTime;

public record BlockResponseDto(Long memberId, String nickname, String profileImageUrl, LocalDateTime blockedAt) {

	public static BlockResponseDto from(BlockResult result) {
		return new BlockResponseDto(result.memberId(), result.nickname(), result.profileImageUrl(), result.blockedAt());
	}

}
