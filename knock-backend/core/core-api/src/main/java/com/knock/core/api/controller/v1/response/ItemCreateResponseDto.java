package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.item.dto.ItemCreateResult;

public record ItemCreateResponseDto() {
	public static ItemCreateResponseDto of(ItemCreateResult item) {
		return new ItemCreateResponseDto();
	}
}
