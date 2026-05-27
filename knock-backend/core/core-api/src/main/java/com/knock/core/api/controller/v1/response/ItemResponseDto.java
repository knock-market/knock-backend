package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;

import java.util.List;

public record ItemResponseDto(Long id, String publicId, String title, String description, Long price, ItemType type,
		ItemStatus status, List<String> imageUrls, Long writerId, String writerNickname, String writerProfileImageUrl,
		String tradeLocationName, String tradeLocationAddress, Double tradeLatitude, Double tradeLongitude) {

	public static ItemResponseDto from(ItemReadResult result) {
		return new ItemResponseDto(result.id(), result.publicId(), result.title(), result.description(), result.price(),
				result.type(), result.status(), result.imageUrls(), result.writerId(), result.writerNickname(),
				result.writerProfileImageUrl(), result.tradeLocationName(), result.tradeLocationAddress(),
				result.tradeLatitude(), result.tradeLongitude());
	}
}
