package com.knock.core.domain.group.dto;

import com.knock.core.api.controller.v1.request.GroupJoinRequestDto;

public record GroupData() {

	public record Create(String name, String description) {
	}

	public record Join(String inviteCode) {
		public static GroupData.Join of(GroupJoinRequestDto request) {
			return new GroupData.Join(request.inviteCode());
		}
	}
}
