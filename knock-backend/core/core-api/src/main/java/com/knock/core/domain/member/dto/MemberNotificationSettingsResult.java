package com.knock.core.domain.member.dto;

import com.knock.storage.db.core.member.Member;

public record MemberNotificationSettingsResult(boolean push, boolean newItems, boolean chat, boolean marketing,
		boolean sound) {

	public static MemberNotificationSettingsResult from(Member member) {
		return new MemberNotificationSettingsResult(member.isNotificationPushEnabled(),
				member.isNotificationNewItemsEnabled(), member.isNotificationChatEnabled(),
				member.isNotificationMarketingEnabled(), member.isNotificationSoundEnabled());
	}

}
