package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.seller.dto.SellerShopResult;

import java.util.List;

public record SellerShopResponseDto(Long sellerId, String sellerName, String sellerNickname,
		String sellerProfileImageUrl, List<ItemSummaryResponseDto> items) {

	public static SellerShopResponseDto from(SellerShopResult result) {
		return new SellerShopResponseDto(result.sellerId(), result.sellerName(), result.sellerNickname(),
				result.sellerProfileImageUrl(), result.items().stream().map(ItemSummaryResponseDto::from).toList());
	}
}
