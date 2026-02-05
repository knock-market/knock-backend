package com.knock.core.support.error.review;

import lombok.Getter;

@Getter
public class ReviewException extends RuntimeException {

	private final transient Object data;

	private final ReviewErrorType reviewErrorType;

	public ReviewException(ReviewErrorType errorType) {
		super(errorType.getMessage());
		this.reviewErrorType = errorType;
		this.data = null;
	}

	public ReviewException(ReviewErrorType errorType, Object data) {
		super(errorType.getMessage());
		this.reviewErrorType = errorType;
		this.data = data;
	}

}
