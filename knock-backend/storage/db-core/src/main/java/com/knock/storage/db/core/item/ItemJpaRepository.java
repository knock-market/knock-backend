package com.knock.storage.db.core.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

	List<Item> findByGroup_Id(Long groupId);

}
