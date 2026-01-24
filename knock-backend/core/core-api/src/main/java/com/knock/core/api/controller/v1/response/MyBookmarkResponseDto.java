package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.bookmark.dto.BookmarkResult;

import java.time.LocalDateTime;

public record MyBookmarkResponseDto(
        Long bookmarkId,
        Long itemId,
        String title,
        Long price,
        String thumbnailUrl,
        LocalDateTime createdAt) {

    public static MyBookmarkResponseDto from(BookmarkResult result) {
        return new MyBookmarkResponseDto(
                result.bookmarkId(),
                result.itemId(),
                result.title(),
                result.price(),
                result.thumbnailUrl(),
                result.createdAt());
    }
}
