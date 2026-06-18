package com.knock.core.enums;

public enum ReportTargetType {

	MEMBER("회원"), ITEM("상품"), RESERVATION("예약"), REVIEW("후기");

	private final String description;

	ReportTargetType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
