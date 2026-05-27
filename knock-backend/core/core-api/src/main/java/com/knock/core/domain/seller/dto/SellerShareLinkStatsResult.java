package com.knock.core.domain.seller.dto;

import com.knock.storage.db.core.seller.SellerShareLink;

import java.time.LocalDateTime;

public record SellerShareLinkStatsResult(String token, LocalDateTime expiresAt, boolean active, Long clickCount,
		Long useCount, LocalDateTime createdAt) {

	public static SellerShareLinkStatsResult from(SellerShareLink shareLink) {
		return new SellerShareLinkStatsResult(shareLink.getToken(), shareLink.getExpiresAt(), shareLink.isActive(),
				shareLink.getClickCount(), shareLink.getUseCount(), shareLink.getCreatedAt());
	}
}
