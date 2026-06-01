package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ItemType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record ItemCreateRequestDto(@NotBlank(message = "Title is required") String title,
		@NotBlank(message = "Description is required") String description,
		@NotNull(message = "Price is required") @PositiveOrZero(message = "Price must be zero or positive") Long price,
		@NotNull(message = "Item type is required") ItemType itemType,
		@NotNull(message = "Image URLs are required") List<String> imageUrls,
		@NotBlank(message = "Trade location name is required") String tradeLocationName,
		@NotBlank(message = "Trade location address is required") String tradeLocationAddress,
		@NotNull(message = "Trade latitude is required") @DecimalMin(value = "-90.0",
				message = "Trade latitude must be at least -90") @DecimalMax(value = "90.0",
						message = "Trade latitude must be at most 90") Double tradeLatitude,
		@NotNull(message = "Trade longitude is required") @DecimalMin(value = "-180.0",
				message = "Trade longitude must be at least -180") @DecimalMax(value = "180.0",
						message = "Trade longitude must be at most 180") Double tradeLongitude) {
	public ItemCreateRequestDto(String title, String description, Long price, ItemType itemType,
			List<String> imageUrls) {
		this(title, description, price, itemType, imageUrls, null, null, null, null);
	}
}
