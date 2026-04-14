package com.knock.storage.db.core.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

	private final ItemJpaRepository itemJpaRepository;

	private final ItemImageRepository itemImageRepository;

	@Override
	public Item save(Item item, List<String> imageUrls) {
		Item savedItem = itemJpaRepository.save(item);

		// 연관된 이미지 모두 저장
		if (imageUrls != null && !imageUrls.isEmpty()) {
			itemImageRepository.saveAll(imageUrls, savedItem);
		}

		return savedItem;
	}

	@Override
	public Optional<Item> findById(Long id) {
		return itemJpaRepository.findById(id);
	}

	@Override
	public List<Object[]> findByGroupIdWithLikes(Long groupId) {
		return itemJpaRepository.findItemsWithLikesByGroupId(groupId);
	}

	@Override
	public List<Object[]> findByMemberIdWithLikes(Long memberId) {
		return itemJpaRepository.findItemsWithLikesByMemberId(memberId);
	}

	@Override
	public List<Object[]> findAllWithLikes() {
		return itemJpaRepository.findAllItemsWithLikes();
	}

	@Override
	public void delete(Item item) {
		itemJpaRepository.delete(item);
	}

	@Override
	public Optional<Item> findByIdWithImages(Long itemId) {
		return itemJpaRepository.findByIdWithImages(itemId);
	}

	@Override
	public void increaseViewCountById(Long itemId) {
		itemJpaRepository.increaseViewCountById(itemId);
	}

}
