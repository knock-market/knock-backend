package com.knock.storage.db.core.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

	Item save(Item item, List<String> imageUrls);

	Optional<Item> findById(Long id);

	List<Item> findByGroupId(Long groupId);

	List<Item> findByMemberId(Long memberId);

	Optional<Item> findByIdWithImages(Long itemId);

	void delete(Item item);

	void increaseViewCountById(Long itemId);
}
