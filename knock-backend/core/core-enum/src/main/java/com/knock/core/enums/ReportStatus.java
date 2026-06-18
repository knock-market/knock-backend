package com.knock.core.enums;

public enum ReportStatus {

	RECEIVED("접수됨"), REVIEWING("검토 중"), RESOLVED("처리 완료"), DISMISSED("기각됨");

	private final String description;

	ReportStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
