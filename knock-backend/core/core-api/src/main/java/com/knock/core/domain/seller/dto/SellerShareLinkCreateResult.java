package com.knock.core.domain.seller.dto;

import java.time.LocalDateTime;

public record SellerShareLinkCreateResult(String token, LocalDateTime expiresAt) {
}
