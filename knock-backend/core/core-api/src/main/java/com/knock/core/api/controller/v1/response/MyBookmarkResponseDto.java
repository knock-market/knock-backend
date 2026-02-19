package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.bookmark.dto.BookmarkResult;
import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;

import java.time.LocalDateTime;

public record MyBookmarkResponseDto(Long bookmarkId, Long itemId, String title, Long price, ItemType type,
		ItemCategory category, ItemStatus status, String thumbnailUrl, LocalDateTime createdAt,
		LocalDateTime itemCreatedAt) {

	public static MyBookmarkResponseDto from(BookmarkResult result) {
		return new MyBookmarkResponseDto(result.bookmarkId(), result.itemId(), result.title(), result.price(),
				result.type(), result.category(), result.status(), result.thumbnailUrl(), result.createdAt(),
				result.itemCreatedAt());
	}
}
