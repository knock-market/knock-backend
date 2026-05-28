package com.knock.storage.db.core.seller;

import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_share_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerShareLink extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(nullable = false, columnDefinition = "boolean default true")
	private boolean active;

	@Column(name = "click_count", nullable = false, columnDefinition = "bigint default 0")
	private Long clickCount;

	@Column(name = "use_count", nullable = false, columnDefinition = "bigint default 0")
	private Long useCount;

	private SellerShareLink(Member member, String token, LocalDateTime expiresAt) {
		this.member = member;
		this.token = token;
		this.expiresAt = expiresAt;
		this.active = true;
		this.clickCount = 0L;
		this.useCount = 0L;
	}

	public static SellerShareLink create(Member member, String token, LocalDateTime expiresAt) {
		return new SellerShareLink(member, token, expiresAt);
	}

	public boolean isExpired(LocalDateTime now) {
		return expiresAt != null && expiresAt.isBefore(now);
	}

	public boolean isAvailable(LocalDateTime now) {
		return active && !isExpired(now);
	}

	public void recordClick() {
		clickCount++;
	}

	public void recordUse() {
		useCount++;
	}

	public void deactivate() {
		active = false;
	}

}
