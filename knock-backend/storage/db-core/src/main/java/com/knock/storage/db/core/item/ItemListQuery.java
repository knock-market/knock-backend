package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemListSort;
import com.knock.core.enums.ItemStatus;

public record ItemListQuery(String keyword, String location, ItemStatus status, ItemListSort sort, int page, int size) {

	public static ItemListQuery defaultQuery() {
		return new ItemListQuery(null, null, ItemStatus.ON_SALE, ItemListSort.LATEST, 0, 20);
	}

}
