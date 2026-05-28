package com.knock.storage.db.core.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

	Optional<Item> findByPublicId(String publicId);

	@Query("""
			SELECT i, (SELECT img.imageUrl FROM ItemImage img WHERE img.item = i ORDER BY img.id ASC LIMIT 1),
			(SELECT COUNT(b) FROM Bookmark b WHERE b.item = i)
			FROM Item i JOIN FETCH i.member
			WHERE i.member.id = :memberId
			""")
	List<Object[]> findItemsWithLikesByMemberId(Long memberId);

	@Query("""
			SELECT i, (SELECT img.imageUrl FROM ItemImage img WHERE img.item = i ORDER BY img.id ASC LIMIT 1),
			(SELECT COUNT(b) FROM Bookmark b WHERE b.item = i)
			FROM Item i JOIN FETCH i.member
			ORDER BY i.createdAt DESC
			""")
	List<Object[]> findAllItemsWithLikes();

	@Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.member LEFT JOIN FETCH i.images WHERE i.id = :itemId")
	Optional<Item> findByIdWithImages(Long itemId);

	@Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.member LEFT JOIN FETCH i.images WHERE i.publicId = :publicId")
	Optional<Item> findByPublicIdWithImages(String publicId);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :itemId")
	void increaseViewCountById(Long itemId);

}
