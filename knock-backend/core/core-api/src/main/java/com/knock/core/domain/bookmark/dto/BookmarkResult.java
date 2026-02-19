package com.knock.core.domain.bookmark.dto;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.item.Item;

import java.time.LocalDateTime;

public record BookmarkResult(Long bookmarkId, Long itemId, String title, Long price, ItemType type,
		ItemCategory category, ItemStatus status, String thumbnailUrl, LocalDateTime createdAt,
		LocalDateTime itemCreatedAt) {
	public static BookmarkResult from(Bookmark bookmark, String thumbnailUrl) {
		Item item = bookmark.getItem();
		return new BookmarkResult(bookmark.getId(), item.getId(), item.getTitle(), item.getPrice(), item.getType(),
				item.getCategory(), item.getStatus(), thumbnailUrl, bookmark.getCreatedAt(), item.getCreatedAt());
	}
}
