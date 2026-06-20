package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ItemListSort;
import com.knock.core.enums.ItemStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.ItemListQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemListRequestDtoTest {

	@Test
	@DisplayName("query 기본값과 text 정규화")
	void toQuery_defaultsAndNormalizesText() {
		ItemListQuery query = new ItemListRequestDto("  MacBook  ", "  Gangnam  ", null, null, null, null).toQuery();

		assertThat(query.keyword()).isEqualTo("macbook");
		assertThat(query.location()).isEqualTo("gangnam");
		assertThat(query.status()).isEqualTo(ItemStatus.ON_SALE);
		assertThat(query.sort()).isEqualTo(ItemListSort.LATEST);
		assertThat(query.page()).isZero();
		assertThat(query.size()).isEqualTo(20);
	}

	@Test
	@DisplayName("blank keyword/location은 absent로 처리")
	void toQuery_blankTextIsAbsent() {
		ItemListQuery query = new ItemListRequestDto("   ", "", "RESERVED", "PRICE_ASC", 1, 10).toQuery();

		assertThat(query.keyword()).isNull();
		assertThat(query.location()).isNull();
		assertThat(query.status()).isEqualTo(ItemStatus.RESERVED);
		assertThat(query.sort()).isEqualTo(ItemListSort.PRICE_ASC);
		assertThat(query.page()).isOne();
		assertThat(query.size()).isEqualTo(10);
	}

	@Test
	@DisplayName("invalid query는 validation error")
	void toQuery_invalidQuery() {
		String tooLong = "a".repeat(101);

		assertValidationError(new ItemListRequestDto(tooLong, null, null, null, null, null));
		assertValidationError(new ItemListRequestDto(null, null, "UNKNOWN", null, null, null));
		assertValidationError(new ItemListRequestDto(null, null, null, "UNKNOWN", null, null));
		assertValidationError(new ItemListRequestDto(null, null, null, null, -1, null));
		assertValidationError(new ItemListRequestDto(null, null, null, null, null, 51));
	}

	private void assertValidationError(ItemListRequestDto request) {
		assertThatThrownBy(request::toQuery).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.VALIDATION_ERROR);
	}

}
