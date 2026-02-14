package com.knock.core.domain.member.dto;

public record MemberNotificationSettingsUpdateData(boolean push, boolean newItems, boolean chat, boolean marketing,
		boolean sound) {
}
