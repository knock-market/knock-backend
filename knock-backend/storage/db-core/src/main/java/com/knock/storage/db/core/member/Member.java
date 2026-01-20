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

	@Column(name = "manner_temperature")
	private Double mannerTemperature;

	@Builder
	public Member(String email, String password, String name, String nickname, String profileImageUrl,
			String provider, String providerId) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.provider = provider;
		this.providerId = providerId;
		this.mannerTemperature = 36.5;
	}

	public void updateProfile(String nickname, String profileImageUrl) {
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}

}