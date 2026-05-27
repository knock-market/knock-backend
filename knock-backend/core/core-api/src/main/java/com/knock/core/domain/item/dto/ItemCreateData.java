package com.knock.core.domain.item.dto;

import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemCreateData(String title, String description, Long price, ItemType type, List<String> imageUrls,
		String tradeLocationName, String tradeLocationAddress, Double tradeLatitude, Double tradeLongitude) {
	public ItemCreateData(String title, String description, Long price, ItemType type, List<String> imageUrls) {
		this(title, description, price, type, imageUrls, null, null, null, null);
	}

	public static ItemCreateData of(ItemCreateRequestDto request) {
		return new ItemCreateData(request.title(), request.description(), request.price(), request.itemType(),
				request.imageUrls(), request.tradeLocationName(), request.tradeLocationAddress(),
				request.tradeLatitude(), request.tradeLongitude());
	}
}
