package com.knock.core.domain.group.dto;

import com.knock.core.api.controller.v1.request.GroupJoinRequestDto;

public record GroupJoinData(String inviteCode) {
	public static GroupJoinData of(GroupJoinRequestDto request) {
		return new GroupJoinData(request.inviteCode());
	}
}
