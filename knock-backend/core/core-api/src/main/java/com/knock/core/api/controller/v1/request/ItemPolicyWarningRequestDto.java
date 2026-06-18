package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.ItemType;
import jakarta.validation.constraints.NotBlank;

public record ItemPolicyWarningRequestDto(@NotBlank(message = "Title is required") String title,
		@NotBlank(message = "Description is required") String description, ItemType itemType) {
}
