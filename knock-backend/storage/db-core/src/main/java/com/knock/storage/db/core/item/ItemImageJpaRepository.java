package com.knock.storage.db.core.item;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ItemImageJpaRepository extends JpaRepository<ItemImage, Long> {

	List<ItemImage> findAllByItem(Item item);

}
