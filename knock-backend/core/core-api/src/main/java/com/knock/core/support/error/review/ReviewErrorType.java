package com.knock.core.support.error.review;

import com.knock.core.support.error.ErrorCode;
import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum ReviewErrorType {

	DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "알 수 없는 에러가 발생했습니다.", LogLevel.ERROR),
	NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, ErrorCode.E404, "리뷰를 찾을 수 없습니다.", LogLevel.INFO);

	private final HttpStatus status;

	private final ErrorCode code;

	private final String message;

	private final LogLevel logLevel;

	ReviewErrorType(HttpStatus status, ErrorCode code, String message, LogLevel logLevel) {
		this.status = status;
		this.code = code;
		this.message = message;
		this.logLevel = logLevel;
	}

}
