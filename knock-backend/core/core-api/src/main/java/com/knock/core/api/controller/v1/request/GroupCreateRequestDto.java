package com.knock.core.api.controller.v1.request;

import jakarta.validation.constraints.NotBlank;

public record GroupCreateRequestDto(@NotBlank(message = "Name is required") String name, String description) {
}
