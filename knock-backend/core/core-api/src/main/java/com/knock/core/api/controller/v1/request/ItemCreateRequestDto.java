package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemCreateRequestDto(Long groupId, String title, String description, Long price, ItemType itemType,
		ItemCategory category, List<String> imageUrls, String tradeLocationName, String tradeLocationAddress,
		Double tradeLatitude, Double tradeLongitude) {
	public ItemCreateRequestDto(Long groupId, String title, String description, Long price, ItemType itemType,
			ItemCategory category, List<String> imageUrls) {
		this(groupId, title, description, price, itemType, category, imageUrls, null, null, null, null);
	}
}
