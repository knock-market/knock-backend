package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.group.dto.GroupInviteCodeResult;

import java.time.LocalDateTime;

public record GroupInviteCodeResponseDto(String inviteCode, LocalDateTime expiresAt) {
	public static GroupInviteCodeResponseDto of(GroupInviteCodeResult result) {
		return new GroupInviteCodeResponseDto(result.inviteCode(), result.expireAt());
	}
}
