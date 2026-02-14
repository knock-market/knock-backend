package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.group.dto.GroupResult;

public record GroupCreateResponseDto(Long id, String name, String description, Long memberCount, String profileImageUrl,
		String inviteCode) {

	public static GroupCreateResponseDto from(GroupResult group) {
		return new GroupCreateResponseDto(group.id(), group.name(), group.description(), group.memberCount(),
				group.profileImageUrl(), group.inviteCode());
	}

}
