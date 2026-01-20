package com.knock.core.domain.group.dto;

import java.time.LocalDateTime;

public record GroupInviteCodeResult(String inviteCode, LocalDateTime expireAt) {
}
