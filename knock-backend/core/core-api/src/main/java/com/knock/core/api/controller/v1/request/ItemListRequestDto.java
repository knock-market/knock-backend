package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ItemListSort;
import com.knock.core.enums.ItemStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.ItemListQuery;

import java.util.Locale;

public record ItemListRequestDto(String keyword, String location, String status, String sort, Integer page,
		Integer size) {

	private static final int MAX_TEXT_LENGTH = 100;

	private static final int DEFAULT_PAGE = 0;

	private static final int DEFAULT_SIZE = 20;

	private static final int MAX_SIZE = 50;

	public ItemListQuery toQuery() {
		int normalizedPage = normalizePage();
		int normalizedSize = normalizeSize();
		return new ItemListQuery(normalizeText(keyword), normalizeText(location), parseStatus(), parseSort(),
				normalizedPage, normalizedSize);
	}

	private String normalizeText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		if (trimmed.length() > MAX_TEXT_LENGTH) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
		return trimmed.toLowerCase(Locale.ROOT);
	}

	private ItemStatus parseStatus() {
		if (status == null || status.isBlank()) {
			return ItemStatus.ON_SALE;
		}
		try {
			return ItemStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
	}

	private ItemListSort parseSort() {
		if (sort == null || sort.isBlank()) {
			return ItemListSort.LATEST;
		}
		try {
			return ItemListSort.valueOf(sort.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
	}

	private int normalizePage() {
		int value = page == null ? DEFAULT_PAGE : page;
		if (value < 0) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
		return value;
	}

	private int normalizeSize() {
		int value = size == null ? DEFAULT_SIZE : size;
		if (value < 1 || value > MAX_SIZE) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
		return value;
	}

}
