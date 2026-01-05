package com.knock.core.enums;

public enum ReservationStatus {

	WAITING("승인 대기"), // 사용자가 예약 버튼 누름
	APPROVED("예약 승인"), // 판매자가 이 사람을 선택함
	COMPLETED("수령 완료"), // 물건 전달 끝
	CANCELED("취소됨");

	private final String description;

	ReservationStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
