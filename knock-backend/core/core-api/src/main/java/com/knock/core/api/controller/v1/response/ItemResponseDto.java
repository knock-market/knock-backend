package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemResponseDto(Long id, String title, String description, Long price, ItemType type,
		ItemCategory category, ItemStatus status, List<String> imageUrls, Long writerId) {

	public static ItemResponseDto from(ItemReadResult result) {
		return new ItemResponseDto(result.id(), result.title(), result.description(), result.price(), result.type(),
				result.category(), result.status(), result.imageUrls(), result.writerId());
	}
}
