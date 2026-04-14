package com.knock.core.domain.item.dto;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemImage;

import java.util.List;

public record ItemReadResult(Long id, String title, String description, Long price, ItemType type,
		ItemCategory category, ItemStatus status, List<String> imageUrls, Long writerId, String writerNickname,
		String writerProfileImageUrl, String tradeLocationName, String tradeLocationAddress, Double tradeLatitude,
		Double tradeLongitude) {
	public ItemReadResult(Long id, String title, String description, Long price, ItemType type,
			ItemCategory category, ItemStatus status, List<String> imageUrls, Long writerId, String writerNickname,
			String writerProfileImageUrl) {
		this(id, title, description, price, type, category, status, imageUrls, writerId, writerNickname,
				writerProfileImageUrl, null, null, null, null);
	}

	public static ItemReadResult from(Item item, List<ItemImage> images) {
		return new ItemReadResult(item.getId(), item.getTitle(), item.getDescription(), item.getPrice(), item.getType(),
				item.getCategory(), item.getStatus(), images.stream().map(ItemImage::getImageUrl).toList(),
				item.getMember().getId(), item.getMember().getNickname(), item.getMember().getProfileImageUrl(),
				item.getTradeLocationName(), item.getTradeLocationAddress(), item.getTradeLatitude(),
				item.getTradeLongitude());
	}
}
