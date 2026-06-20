package com.knock.storage.db.core.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

	Item save(Item item, List<String> imageUrls);

	Optional<Item> findById(Long id);

	Optional<Item> findByPublicId(String publicId);

	List<Object[]> findOwnerInventoryWithLikes(Long memberId);

	List<Object[]> findPublicListingsByMemberIdWithLikes(Long memberId, ItemListQuery query);

	default List<Object[]> findPublicListingsWithLikes() {
		return findPublicListingsWithLikes(ItemListQuery.defaultQuery());
	}

	List<Object[]> findPublicListingsWithLikes(ItemListQuery query);

	Optional<Item> findByIdWithImages(Long itemId);

	Optional<Item> findByPublicIdWithImages(String publicId);

	void delete(Item item);

	void increaseViewCountById(Long itemId);

}
