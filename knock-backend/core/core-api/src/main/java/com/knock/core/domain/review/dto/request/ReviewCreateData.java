package com.knock.core.domain.review.dto.request;

import com.knock.core.api.controller.v1.request.ReviewCreateRequest;

public record ReviewCreateData(Long itemId, String content, int score) {
	public static ReviewCreateData from(ReviewCreateRequest request) {
		return new ReviewCreateData(request.itemId(), request.content(), request.score());
	}
}
