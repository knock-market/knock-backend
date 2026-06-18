package com.knock.core.support.error;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

	// default
	DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.",
			LogLevel.ERROR),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid input value.", LogLevel.WARN),
	NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "요청한 리소스를 찾을 수 없습니다.", LogLevel.INFO),

	// member
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, ErrorCode.A002, "비밀번호가 일치하지 않습니다.", LogLevel.DEBUG),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, ErrorCode.A003, "인증에 실패했습니다.", LogLevel.WARN),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, ErrorCode.M001, "이미 존재하는 이메일입니다.", LogLevel.WARN),
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.M002, "유저를 찾을 수 없습니다.", LogLevel.WARN),

	// item
	ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.I001, "상품을 찾을 수 없습니다.", LogLevel.WARN),

	// notification
	NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.N001, "알림을 찾을 수 없습니다.", LogLevel.WARN),

	// location
	LOCATION_SEARCH_UNAVAILABLE(HttpStatus.BAD_GATEWAY, ErrorCode.L001, "위치 검색을 사용할 수 없습니다.", LogLevel.WARN),

	FORBIDDEN(HttpStatus.FORBIDDEN, ErrorCode.E403, "접근 권한이 없습니다.", LogLevel.WARN),

	// Reservation
	RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.RE001, "예약을 찾을 수 없습니다.", LogLevel.WARN),
	RESERVATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, ErrorCode.RE002, "이미 예약된 시간입니다.", LogLevel.WARN),
	RESERVATION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, ErrorCode.RE003, "완료된 예약에 대해서만 후기를 작성할 수 있습니다.", LogLevel.WARN),

	// Review
	DUPLICATE_REVIEW(HttpStatus.BAD_REQUEST, ErrorCode.RV001, "중복된 리뷰 요청입니다.", LogLevel.WARN),

	// Block
	SELF_BLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, ErrorCode.B001, "자기 자신은 차단할 수 없습니다.", LogLevel.WARN),
	BLOCKED_INTERACTION(HttpStatus.FORBIDDEN, ErrorCode.B002, "차단 관계에서는 이 상호작용을 할 수 없습니다.", LogLevel.WARN),

	// Report
	SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, ErrorCode.RP001, "자기 자신 또는 자신의 콘텐츠는 신고할 수 없습니다.", LogLevel.WARN),
	DUPLICATE_REPORT(HttpStatus.CONFLICT, ErrorCode.RP002, "이미 접수된 신고입니다.", LogLevel.WARN);

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

}
