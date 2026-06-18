package com.knock.core.enums;

public enum BlockStatus {

	ACTIVE("차단 중");

	private final String description;

	BlockStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
