package com.knock.core.domain.seller.dto;

import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.storage.db.core.member.Member;

import java.util.List;

public record SellerShopResult(Long sellerId, String sellerName, String sellerNickname, String sellerProfileImageUrl,
		List<ItemListResult> items) {

	public static SellerShopResult from(Member member, List<ItemListResult> items) {
		return new SellerShopResult(member.getId(), member.getName(), member.getNickname(), member.getProfileImageUrl(),
				items);
	}
}
