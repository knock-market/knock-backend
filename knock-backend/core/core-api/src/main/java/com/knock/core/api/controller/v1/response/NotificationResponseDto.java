package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.notification.dto.NotificationResult;
import com.knock.core.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto(Long id, NotificationType notificationType, String content, String relatedUrl,
		boolean isRead, LocalDateTime createdAt) {

	public static NotificationResponseDto from(NotificationResult result) {
		return new NotificationResponseDto(result.id(), result.notificationType(), result.content(),
				result.relatedUrl(), result.isRead(), result.createdAt());
	}
}
