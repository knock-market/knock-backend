package com.knock.core.domain.item.dto;

import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemCreateData(String title, String description, Long price, ItemType type, ItemCategory category,
		List<String> imageUrls) {
	public static ItemCreateData of(ItemCreateRequestDto request) {
		return new ItemCreateData(
				request.title(),
				request.description(),
				request.price(),
				request.itemType(),
				request.category(),
				request.imageUrls());
	}
}
