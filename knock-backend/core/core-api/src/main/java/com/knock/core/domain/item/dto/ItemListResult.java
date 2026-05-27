package com.knock.core.domain.item.dto;

import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.item.Item;

import java.time.LocalDateTime;

public record ItemListResult(Long id, String publicId, String title, Long price, ItemType type, ItemStatus status,
		String thumbnailUrl, Long writerId, String writerNickname, String writerProfileImageUrl, Long likesCount,
		java.time.LocalDateTime postedAt, String tradeLocationName, String tradeLocationAddress, Double tradeLatitude,
		Double tradeLongitude) {
	public ItemListResult(Long id, String publicId, String title, Long price, ItemType type, ItemStatus status,
			String thumbnailUrl, Long writerId, Long likesCount, LocalDateTime postedAt) {
		this(id, publicId, title, price, type, status, thumbnailUrl, writerId, null, null, likesCount, postedAt, null,
				null, null, null);
	}

	public static ItemListResult from(Item item, String thumbnailUrl, Long likesCount) {
		return new ItemListResult(item.getId(), item.getPublicId(), item.getTitle(), item.getPrice(), item.getType(),
				item.getStatus(), thumbnailUrl, item.getMember().getId(), item.getMember().getNickname(),
				item.getMember().getProfileImageUrl(), likesCount, item.getCreatedAt(), item.getTradeLocationName(),
				item.getTradeLocationAddress(), item.getTradeLatitude(), item.getTradeLongitude());
	}
}
