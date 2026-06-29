package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.seller.dto.SellerAccessMembershipResult;

import java.time.LocalDateTime;

public record SellerAccessMembershipResponseDto(Long sellerId, Long memberId, String status, LocalDateTime createdAt) {

	public static SellerAccessMembershipResponseDto from(SellerAccessMembershipResult result) {
		return new SellerAccessMembershipResponseDto(result.sellerId(), result.memberId(), result.status(),
				result.createdAt());
	}

}
