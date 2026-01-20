package com.knock.core.domain.member.dto;

import com.knock.storage.db.core.member.Member;

public record MemberResult(String email, String name, String nickname, String profileImageUrl, String provider) {
	public static MemberResult of(Member member) {
		return new MemberResult(
				member.getEmail(),
				member.getName(),
				member.getNickname(),
				member.getProfileImageUrl(),
				member.getProvider());
	}
}
