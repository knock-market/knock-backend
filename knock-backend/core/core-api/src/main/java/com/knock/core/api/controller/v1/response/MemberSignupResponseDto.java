package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.member.dto.MemberSignupResult;

public record MemberSignupResponseDto(String email, String name, String nickname, String profileImageUrl,
		String provider) {
	public static MemberSignupResponseDto of(MemberSignupResult result) {
		return new MemberSignupResponseDto(result.email(), result.name(), result.nickname(), result.profileImageUrl(),
				result.provider());
	}
}
