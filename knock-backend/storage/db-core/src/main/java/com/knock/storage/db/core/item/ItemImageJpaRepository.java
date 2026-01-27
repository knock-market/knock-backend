package com.knock.storage.db.core.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ItemImageJpaRepository extends JpaRepository<ItemImage, Long> {

	List<ItemImage> findAllByItem(Item item);

}
