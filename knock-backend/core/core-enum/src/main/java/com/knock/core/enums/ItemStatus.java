package com.knock.core.enums;

public enum ItemStatus {

	ON_SALE("판매 중"), RESERVED("예약 중"), // 예약자가 확정된 상태
	SOLD("거래 완료");

	private final String description;

	ItemStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
