package com.knock.core.enums;

public enum ItemCategory {

	DIGITAL_DEVICE("가전/디지털"), FURNITURE("가구/인테리어"), CLOTHING("의류"), BOOKS("도서"), ETC("기타");

	private final String description;

	ItemCategory(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
