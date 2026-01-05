package com.knock.storage.db.core.item;

import java.util.List;

public interface ItemImageRepository {

	ItemImage save(ItemImage itemImage);

	void saveAll(List<String> imageUrls, Item item);

	List<ItemImage> findByItem(Item item);

	void delete(ItemImage itemImage);

}
