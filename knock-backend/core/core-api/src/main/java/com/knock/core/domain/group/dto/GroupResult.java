package com.knock.core.domain.group.dto;

import com.knock.storage.db.core.group.Group;

public record GroupResult(Long id, String name, String description, String inviteCode, Long ownerId, Long memberCount,
		String profileImageUrl) {
	public static GroupResult from(Group group, Long memberCount) {
		return new GroupResult(group.getId(), group.getName(), group.getDescription(), group.getInviteCode(),
				group.getOwnerId(), memberCount, group.getCoverImage());
	}
}
