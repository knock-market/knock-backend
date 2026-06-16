package com.knock.core.domain.item.dto;

import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemCreateData(String title, String description, Long price, ItemType type, List<String> imageUrls,
		String tradeLocationName, String tradeLocationAddress, Double tradeLatitude, Double tradeLongitude) {
}
