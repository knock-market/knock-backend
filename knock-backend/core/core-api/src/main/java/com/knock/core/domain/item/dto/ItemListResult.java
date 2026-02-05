package com.knock.core.domain.item.dto;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.item.Item;

public record ItemListResult(Long id, String title, Long price, ItemType type, ItemCategory category, ItemStatus status,
		String thumbnailUrl, Long writerId, Long likesCount, java.time.LocalDateTime postedAt) {
	public static ItemListResult from(Item item, String thumbnailUrl, Long likesCount) {
		return new ItemListResult(item.getId(), item.getTitle(), item.getPrice(), item.getType(), item.getCategory(),
				item.getStatus(), thumbnailUrl, item.getMember().getId(), likesCount, item.getCreatedAt());
	}
}
