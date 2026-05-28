package com.knock.storage.db.core.member;

import com.knock.storage.db.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE member SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Member extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String nickname;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(nullable = false)
	private String provider; // kakao, google etc.

	@Column(name = "provider_id")
	private String providerId;

	@Column(name = "notification_push_enabled", nullable = false)
	private boolean notificationPushEnabled;

	@Column(name = "notification_new_items_enabled", nullable = false)
	private boolean notificationNewItemsEnabled;

	@Column(name = "notification_chat_enabled", nullable = false)
	private boolean notificationChatEnabled;

	@Column(name = "notification_marketing_enabled", nullable = false)
	private boolean notificationMarketingEnabled;

	@Column(name = "notification_sound_enabled", nullable = false)
	private boolean notificationSoundEnabled;

	@Builder
	public Member(String email, String password, String name, String nickname, String profileImageUrl, String provider,
			String providerId) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.provider = provider;
		this.providerId = providerId;
		this.notificationPushEnabled = true;
		this.notificationNewItemsEnabled = true;
		this.notificationChatEnabled = true;
		this.notificationMarketingEnabled = false;
		this.notificationSoundEnabled = true;
	}

	public static Member create(String email, String name, String password, String nickname, String provider) {
		return Member.builder()
			.email(email)
			.name(name)
			.password(password)
			.nickname(nickname)
			.provider(provider)
			.build();
	}

	public void updateProfile(String nickname, String profileImageUrl) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}

	public void updateNotificationSettings(boolean pushEnabled, boolean newItemsEnabled, boolean chatEnabled,
			boolean marketingEnabled, boolean soundEnabled) {
		this.notificationPushEnabled = pushEnabled;
		this.notificationNewItemsEnabled = newItemsEnabled;
		this.notificationChatEnabled = chatEnabled;
		this.notificationMarketingEnabled = marketingEnabled;
		this.notificationSoundEnabled = soundEnabled;
	}

}
