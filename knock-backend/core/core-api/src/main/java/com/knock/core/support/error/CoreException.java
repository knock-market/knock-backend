package com.knock.core.support.error;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

	private final ErrorType errorType;

	private final Object data;

	public CoreException(ErrorType errorType) {
		super(errorType.getMessage());
		this.errorType = errorType;
		this.data = null;
	}

	public CoreException(ErrorType errorType, Object data) {
		super(errorType.getMessage());
		this.errorType = errorType;
		this.data = data;
	}

}
