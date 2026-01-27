package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;

public record ItemSummaryResponseDto(Long id, String title, Long price, ItemType type, ItemCategory category,
		ItemStatus status, String thumbnailUrl, Long writerId) {

	public static ItemSummaryResponseDto from(ItemListResult result) {
		return new ItemSummaryResponseDto(result.id(), result.title(), result.price(), result.type(), result.category(),
				result.status(), result.thumbnailUrl(), result.writerId());
	}
}
