package com.knock.core.enums;

public enum NotificationType {

	RESERVATION_CREATED("예약 요청"), RESERVATION_APPROVED("예약 승인"), RESERVATION_COMPLETED("수령 완료"),
	RESERVATION_CANCELED("예약 취소"), ITEM_COMMENT("물건 댓글");

	private final String description;

	NotificationType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
