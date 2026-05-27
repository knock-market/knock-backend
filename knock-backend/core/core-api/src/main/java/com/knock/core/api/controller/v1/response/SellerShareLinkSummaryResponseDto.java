package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.seller.dto.SellerShareLinkStatsResult;

import java.time.LocalDateTime;

public record SellerShareLinkSummaryResponseDto(String token, String path, LocalDateTime expiresAt, boolean active,
		Long clickCount, Long useCount, LocalDateTime createdAt) {

	public static SellerShareLinkSummaryResponseDto from(SellerShareLinkStatsResult result) {
		return new SellerShareLinkSummaryResponseDto(result.token(), "/shop/" + result.token(), result.expiresAt(),
				result.active(), result.clickCount(), result.useCount(), result.createdAt());
	}
}
