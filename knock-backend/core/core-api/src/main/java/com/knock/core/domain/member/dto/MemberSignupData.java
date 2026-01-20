package com.knock.core.domain.member.dto;

import com.knock.core.api.controller.v1.request.MemberSignupRequestDto;

public record MemberSignupData(String email, String password, String name, String nickname, String provider, String profileImageUrl) {
	public static MemberSignupData of(MemberSignupRequestDto request) {
		return new MemberSignupData(request.email(), request.password(), request.name(), request.nickname(), "NONE", request.profileImageUrl());
	}
}
