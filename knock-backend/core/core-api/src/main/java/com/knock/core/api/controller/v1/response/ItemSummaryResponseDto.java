package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;

public record ItemSummaryResponseDto(Long id, String publicId, String title, Long price, ItemType type,
		ItemStatus status, String thumbnailUrl, Long writerId, String writerNickname, String writerProfileImageUrl,
		Long likesCount, java.time.LocalDateTime postedAt, String tradeLocationName, String tradeLocationAddress,
		Double tradeLatitude, Double tradeLongitude) {

	public static ItemSummaryResponseDto from(ItemListResult result) {
		return new ItemSummaryResponseDto(result.id(), result.publicId(), result.title(), result.price(), result.type(),
				result.status(), result.thumbnailUrl(), result.writerId(), result.writerNickname(),
				result.writerProfileImageUrl(), result.likesCount(), result.postedAt(), result.tradeLocationName(),
				result.tradeLocationAddress(), result.tradeLatitude(), result.tradeLongitude());
	}
}
