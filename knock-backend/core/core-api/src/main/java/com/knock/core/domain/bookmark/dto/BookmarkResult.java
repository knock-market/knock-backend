package com.knock.core.domain.bookmark.dto;

import com.knock.storage.db.core.bookmark.Bookmark;

import java.time.LocalDateTime;

public record BookmarkResult(Long bookmarkId, Long itemId, String title, Long price, String thumbnailUrl,
		LocalDateTime createdAt) {
	public static BookmarkResult from(Bookmark bookmark, String thumbnailUrl) {
		return new BookmarkResult(bookmark.getId(), bookmark.getItem().getId(), bookmark.getItem().getTitle(),
				bookmark.getItem().getPrice(), thumbnailUrl, bookmark.getCreatedAt());
	}
}
