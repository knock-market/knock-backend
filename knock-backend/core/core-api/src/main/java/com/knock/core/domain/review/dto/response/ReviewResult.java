package com.knock.core.domain.review.dto.response;

import com.knock.storage.db.core.review.Review;

public record ReviewResult(Long reviewId, String content, Integer score) {
	public static ReviewResult from(Review review) {
		return new ReviewResult(review.getId(), review.getContent(), review.getScore());
	}
}
