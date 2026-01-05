package com.knock.core.api.controller.v1.request;

import com.knock.core.enums.InviteDuration;

public record InviteCodeRequestDto(InviteDuration duration) {
}
