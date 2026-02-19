package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.member.dto.MemberNotificationSettingsResult;

public record NotificationSettingsResponseDto(boolean push, boolean newItems, boolean chat, boolean marketing,
		boolean sound) {

	public static NotificationSettingsResponseDto from(MemberNotificationSettingsResult result) {
		return new NotificationSettingsResponseDto(result.push(), result.newItems(), result.chat(), result.marketing(),
				result.sound());
	}

}
