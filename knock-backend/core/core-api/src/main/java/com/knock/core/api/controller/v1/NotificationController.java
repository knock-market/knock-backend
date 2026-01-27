package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.response.NotificationResponseDto;
import com.knock.core.domain.notification.NotificationService;
import com.knock.core.domain.notification.dto.NotificationResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/api/v1/notifications")
	public ApiResponse<List<NotificationResponseDto>> getMyNotifications(
			@AuthenticationPrincipal MemberPrincipal principal) {
		List<NotificationResult> results = notificationService.getMyNotifications(principal.getMemberId());
		List<NotificationResponseDto> response = results.stream().map(NotificationResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@PatchMapping("/api/v1/notifications/{id}/read")
	public ApiResponse<Void> markAsRead(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long id) {
		notificationService.markAsRead(principal.getMemberId(), id);
		return ApiResponse.success(null);
	}

}
