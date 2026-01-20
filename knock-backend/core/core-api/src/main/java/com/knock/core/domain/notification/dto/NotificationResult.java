package com.knock.core.domain.notification.dto;

import com.knock.core.enums.NotificationType;
import com.knock.storage.db.core.notification.Notification;

import java.time.LocalDateTime;

public record NotificationResult(Long id, NotificationType notificationType, String content, String relatedUrl, boolean isRead, LocalDateTime createdAt) {
	public static NotificationResult from(Notification notification) {
		return new NotificationResult(notification.getId(), notification.getNotificationType(),
				notification.getContent(), notification.getRelatedUrl(), notification.isRead(),
				notification.getCreatedAt());
	}
}
