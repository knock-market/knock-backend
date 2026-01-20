package com.knock.core.api.controller.v1.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberSignupRequestDto(
		@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
		@NotBlank(message = "Name is required") String name,
		@NotBlank(message = "Password is required") String password,
		@NotBlank(message = "Nickname is required") String nickname,
		String profileImageUrl) {
}
