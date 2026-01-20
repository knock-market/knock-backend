package com.knock.core.domain.member.dto;

import com.knock.storage.db.core.member.Member;

public record MemberSignupResult(String email, String name, String nickname, String profileImageUrl, String provider) {
	public static MemberSignupResult of(Member member) {
		return new MemberSignupResult(
				member.getEmail(),
				member.getName(),
				member.getNickname(),
				member.getProfileImageUrl(),
				member.getProvider());
	}
}
