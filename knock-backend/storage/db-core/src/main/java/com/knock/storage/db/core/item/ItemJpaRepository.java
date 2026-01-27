package com.knock.storage.db.core.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

	@Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.group.id = :groupId")
	List<Item> findAllByGroup_Id(Long groupId);

	@Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.member.id = :memberId")
	List<Item> findAllByMember_Id(Long memberId);

	@Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.id = :itemId")
	Optional<Item> findByIdWithImages(Long itemId);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :itemId")
	void increaseViewCountById(Long itemId);

}
