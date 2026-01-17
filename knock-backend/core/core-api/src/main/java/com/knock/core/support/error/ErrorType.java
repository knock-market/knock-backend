package com.knock.core.support.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

public enum ErrorType {

	DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.", LogLevel.ERROR),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid input value.", LogLevel.WARN),
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, ErrorCode.A002, "비밀번호가 일치하지 않습니다.", LogLevel.DEBUG),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, ErrorCode.M001, "이미 존재하는 이메일입니다.", LogLevel.WARN),
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.M002, "유저를 찾을 수 없습니다.", LogLevel.WARN),
	INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, ErrorCode.G001, "옳바르지 않은 그룹 초대 코드입니다.", LogLevel.WARN),
	ALREADY_MEMBER(HttpStatus.BAD_REQUEST, ErrorCode.G002, "이미 참여한 그룹입니다.", LogLevel.WARN),
	GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.G003, "그룹을 찾을 수 없습니다.", LogLevel.WARN),
	INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, ErrorCode.G004, "초대 코드가 만료되었습니다.", LogLevel.WARN),
	FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "접근 권한이 없습니다.", LogLevel.WARN);

	private final HttpStatus status;

	private final ErrorCode code;

	private final String message;

	private final LogLevel logLevel;

	ErrorType(HttpStatus status, ErrorCode code, String message, LogLevel logLevel) {
		this.status = status;
		this.code = code;
		this.message = message;
		this.logLevel = logLevel;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public ErrorCode getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

}
