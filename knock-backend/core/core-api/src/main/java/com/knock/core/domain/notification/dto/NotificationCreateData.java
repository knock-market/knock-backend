package com.knock.core.domain.notification.dto;

import com.knock.core.enums.NotificationType;

public record NotificationCreateData(Long memberId, NotificationType notificationType, String content, String relatedUrl) {
}
