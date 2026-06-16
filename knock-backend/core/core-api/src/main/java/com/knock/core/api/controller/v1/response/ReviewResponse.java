package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.review.dto.response.ReviewResult;

public record ReviewResponse(Long id, String content, int score) {
	public static ReviewResponse from(ReviewResult reviewResult) {
		return new ReviewResponse(reviewResult.reviewId(), reviewResult.content(), reviewResult.score());
	}
}
