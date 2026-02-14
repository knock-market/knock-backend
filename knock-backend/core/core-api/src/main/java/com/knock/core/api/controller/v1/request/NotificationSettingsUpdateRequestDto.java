package com.knock.core.api.controller.v1.request;

import com.knock.core.domain.member.dto.MemberNotificationSettingsUpdateData;

public record NotificationSettingsUpdateRequestDto(boolean push, boolean newItems, boolean chat, boolean marketing,
		boolean sound) {

	public MemberNotificationSettingsUpdateData toData() {
		return new MemberNotificationSettingsUpdateData(push, newItems, chat, marketing, sound);
	}

}
