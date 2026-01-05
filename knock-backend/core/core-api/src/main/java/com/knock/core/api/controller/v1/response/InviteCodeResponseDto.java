package com.knock.core.api.controller.v1.response;

import java.time.LocalDateTime;

public record InviteCodeResponseDto(String inviteCode, LocalDateTime expiresAt) {
}
