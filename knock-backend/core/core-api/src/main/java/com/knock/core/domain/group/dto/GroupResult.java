package com.knock.core.domain.group.dto;

import com.knock.storage.db.core.group.Group;

public record GroupResult(Long id, String name, String description, String inviteCode, Long ownerId) {
	public static GroupResult from(Group group) {
		return new GroupResult(group.getId(), group.getName(), group.getDescription(), group.getInviteCode(),
				group.getOwnerId());
	}
}
