package com.knock.core.enums;

public enum ItemType {

	SELL("판매"), GIVE("나눔"), RENT("대여");

	private final String description;

	ItemType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
