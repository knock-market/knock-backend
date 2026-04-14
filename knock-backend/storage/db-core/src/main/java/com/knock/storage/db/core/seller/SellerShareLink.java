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

	private SellerShareLink(Member member, String token, LocalDateTime expiresAt) {
		this.member = member;
		this.token = token;
		this.expiresAt = expiresAt;
	}

	public static SellerShareLink create(Member member, String token, LocalDateTime expiresAt) {
		return new SellerShareLink(member, token, expiresAt);
	}

	public boolean isExpired(LocalDateTime now) {
		return expiresAt != null && expiresAt.isBefore(now);
	}

}
