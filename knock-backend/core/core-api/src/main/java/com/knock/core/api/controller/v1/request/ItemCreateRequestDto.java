package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemCreateRequestDto(String title, String description, Long price, ItemType itemType,
		List<String> imageUrls, String tradeLocationName, String tradeLocationAddress, Double tradeLatitude,
		Double tradeLongitude) {
	public ItemCreateRequestDto(String title, String description, Long price, ItemType itemType,
			List<String> imageUrls) {
		this(title, description, price, itemType, imageUrls, null, null, null, null);
	}
}
