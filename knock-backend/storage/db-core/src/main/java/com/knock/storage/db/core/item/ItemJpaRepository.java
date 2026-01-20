package com.knock.storage.db.core.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

	@org.springframework.data.jpa.repository.Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.group.id = :groupId")
	List<Item> findAllByGroup_Id(Long groupId);

	@org.springframework.data.jpa.repository.Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.images WHERE i.member.id = :memberId")
	List<Item> findAllByMember_Id(Long memberId);

}
