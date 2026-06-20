package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemListSort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

	private final ItemJpaRepository itemJpaRepository;

	private final ItemImageRepository itemImageRepository;

	private final EntityManager entityManager;

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
	public Optional<Item> findByPublicId(String publicId) {
		return itemJpaRepository.findByPublicId(publicId);
	}

	@Override
	public List<Object[]> findOwnerInventoryWithLikes(Long memberId) {
		return itemJpaRepository.findOwnerInventoryWithLikes(memberId);
	}

	@Override
	public List<Object[]> findPublicListingsByMemberIdWithLikes(Long memberId, ItemListQuery query) {
		return findItemsWithLikes(memberId, query);
	}

	@Override
	public List<Object[]> findPublicListingsWithLikes(ItemListQuery query) {
		return findItemsWithLikes(null, query);
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
	public Optional<Item> findByPublicIdWithImages(String publicId) {
		return itemJpaRepository.findByPublicIdWithImages(publicId);
	}

	@Override
	public void increaseViewCountById(Long itemId) {
		itemJpaRepository.increaseViewCountById(itemId);
	}

	private List<Object[]> findItemsWithLikes(Long memberId, ItemListQuery query) {
		String jpql = buildListQuery(memberId, query);
		TypedQuery<Object[]> typedQuery = entityManager.createQuery(jpql, Object[].class);
		bindListParameters(typedQuery, memberId, query);
		typedQuery.setFirstResult(query.page() * query.size());
		typedQuery.setMaxResults(query.size());
		return typedQuery.getResultList();
	}

	private String buildListQuery(Long memberId, ItemListQuery query) {
		StringBuilder jpql = new StringBuilder("""
				SELECT i, (SELECT img.imageUrl FROM ItemImage img WHERE img.item = i ORDER BY img.id ASC LIMIT 1),
				(SELECT COUNT(b) FROM Bookmark b WHERE b.item = i AND b.member.id <> i.member.id)
				FROM Item i JOIN FETCH i.member
				WHERE i.status = :status
				""");
		if (memberId != null) {
			jpql.append(" AND i.member.id = :memberId");
		}
		if (query.keyword() != null) {
			jpql.append(" AND (LOWER(i.title) LIKE :keyword OR LOWER(i.description) LIKE :keyword)");
		}
		if (query.location() != null) {
			jpql.append("""
					 AND (LOWER(i.tradeLocationName) LIKE :location
					 OR LOWER(i.tradeLocationAddress) LIKE :location)
					""");
		}
		jpql.append(orderBy(query.sort()));
		return jpql.toString();
	}

	private void bindListParameters(TypedQuery<Object[]> typedQuery, Long memberId, ItemListQuery query) {
		typedQuery.setParameter("status", query.status());
		if (memberId != null) {
			typedQuery.setParameter("memberId", memberId);
		}
		if (query.keyword() != null) {
			typedQuery.setParameter("keyword", "%" + query.keyword() + "%");
		}
		if (query.location() != null) {
			typedQuery.setParameter("location", "%" + query.location() + "%");
		}
	}

	private String orderBy(ItemListSort sort) {
		return switch (sort) {
			case POPULAR -> """
					 ORDER BY (SELECT COUNT(b2) FROM Bookmark b2 WHERE b2.item = i AND b2.member.id <> i.member.id) DESC,
					 i.createdAt DESC, i.id DESC
					""";
			case PRICE_ASC -> " ORDER BY i.price ASC, i.createdAt DESC, i.id DESC";
			case PRICE_DESC -> " ORDER BY i.price DESC, i.createdAt DESC, i.id DESC";
			case LATEST -> " ORDER BY i.createdAt DESC, i.id DESC";
		};
	}

}
