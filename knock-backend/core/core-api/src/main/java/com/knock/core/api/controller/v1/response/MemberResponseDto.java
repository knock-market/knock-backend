package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.member.dto.MemberResult;

public record MemberResponseDto(String email, String name, String nickname, String profileImageUrl, String provider) {
	public static MemberResponseDto of(MemberResult result) {
		return new MemberResponseDto(result.email(), result.name(), result.nickname(), result.profileImageUrl(),
				result.provider());
	}
}
