package com.knock.core.api.controller.v1.response;

import com.knock.storage.db.core.bookmark.Bookmark;

import java.time.LocalDateTime;

public record BookmarkResponseDto(Long bookmarkId, Long itemId, String title, Long price, String thumbnailUrl,
		LocalDateTime createdAt) {
	public static BookmarkResponseDto from(Bookmark bookmark, String thumbnailUrl) {
		return new BookmarkResponseDto(bookmark.getId(), bookmark.getItem().getId(), bookmark.getItem().getTitle(),
				bookmark.getItem().getPrice(), thumbnailUrl, bookmark.getCreatedAt());
	}
}
