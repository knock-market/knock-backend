package com.knock.core.api.controller.v1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationCreateRequestDto(
		@NotNull(message = "Item ID is required") @Positive(message = "Item ID must be positive") Long itemId) {
}
