package com.knock.core.domain.review.dto.request;

public record ReviewCreateData(Long itemId, String content, int score) {
}
