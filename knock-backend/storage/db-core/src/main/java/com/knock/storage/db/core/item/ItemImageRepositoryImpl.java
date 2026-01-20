package com.knock.storage.db.core.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemImageRepositoryImpl implements ItemImageRepository {

	private final ItemImageJpaRepository jpaRepository;

	@Override
	public ItemImage save(ItemImage itemImage) {
		return jpaRepository.save(itemImage);
	}

	@Override
	public void saveAll(List<String> imageUrls, Item item) {
		if (imageUrls == null || imageUrls.isEmpty()) {
			return;
		}

		List<ItemImage> images = java.util.stream.IntStream.range(0, imageUrls.size())
			.mapToObj(i -> ItemImage.create(item, imageUrls.get(i), i + 1))
			.toList();

		jpaRepository.saveAll(images);
	}

	@Override
	public List<ItemImage> findByItem(Item item) {
		return jpaRepository.findAllByItem(item);
	}

	@Override
	public void delete(ItemImage itemImage) {
		jpaRepository.delete(itemImage);
	}

}
