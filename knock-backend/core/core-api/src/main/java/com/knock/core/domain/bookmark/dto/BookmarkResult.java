package com.knock.core.domain.bookmark.dto;

import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.item.Item;

import java.time.LocalDateTime;

public record BookmarkResult(Long bookmarkId, Long itemId, String itemPublicId, String title, Long price, ItemType type,
		ItemStatus status, String thumbnailUrl, LocalDateTime createdAt, LocalDateTime itemCreatedAt) {
	public static BookmarkResult from(Bookmark bookmark, String thumbnailUrl) {
		Item item = bookmark.getItem();
		return new BookmarkResult(bookmark.getId(), item.getId(), item.getPublicId(), item.getTitle(), item.getPrice(),
				item.getType(), item.getStatus(), thumbnailUrl, bookmark.getCreatedAt(), item.getCreatedAt());
	}
}
