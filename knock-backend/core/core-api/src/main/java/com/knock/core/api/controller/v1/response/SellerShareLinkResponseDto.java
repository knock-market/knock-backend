package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;

import java.time.LocalDateTime;

public record SellerShareLinkResponseDto(String token, String path, LocalDateTime expiresAt) {

	public static SellerShareLinkResponseDto from(SellerShareLinkCreateResult result) {
		return new SellerShareLinkResponseDto(result.token(), "/shop/" + result.token(), result.expiresAt());
	}
}
