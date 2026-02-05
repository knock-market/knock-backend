package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.group.dto.GroupResult;

public record GroupResponseDto(Long id, String name, String description, Long memberCount, String profileImageUrl) {
	public static GroupResponseDto from(GroupResult group) {
		return new GroupResponseDto(group.id(), group.name(), group.description(), group.memberCount(),
				group.profileImageUrl());
	}
}
