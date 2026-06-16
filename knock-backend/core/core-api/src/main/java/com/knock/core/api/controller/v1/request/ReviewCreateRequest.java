package com.knock.core.api.controller.v1.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(@NotNull(message = "Item ID is required") Long itemId,
		@NotBlank(message = "Content is required") String content,
		@NotNull(message = "Score is required") @Min(value = 1, message = "Score must be at least 1") @Max(value = 5,
				message = "Score must be at most 5") Integer score) {
}
