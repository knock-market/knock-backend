package com.knock.core.api.controller.v1.request;

import jakarta.validation.constraints.NotBlank;

public record GroupJoinRequestDto(@NotBlank(message = "Invite code is required") String inviteCode) {
}
