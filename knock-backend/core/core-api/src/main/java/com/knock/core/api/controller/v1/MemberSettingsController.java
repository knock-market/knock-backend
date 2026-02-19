package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.NotificationSettingsUpdateRequestDto;
import com.knock.core.api.controller.v1.response.NotificationSettingsResponseDto;
import com.knock.core.domain.member.MemberService;
import com.knock.core.domain.member.dto.MemberNotificationSettingsResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberSettingsController {

	private final MemberService memberService;

	@GetMapping("/api/v1/members/my/settings/notifications")
	public ApiResponse<NotificationSettingsResponseDto> getNotificationSettings(
			@AuthenticationPrincipal MemberPrincipal principal) {
		MemberNotificationSettingsResult result = memberService.getNotificationSettings(principal.getMemberId());
		return ApiResponse.success(NotificationSettingsResponseDto.from(result));
	}

	@PutMapping("/api/v1/members/my/settings/notifications")
	public ApiResponse<?> updateNotificationSettings(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody NotificationSettingsUpdateRequestDto request) {
		memberService.updateNotificationSettings(principal.getMemberId(), request.toData());
		return ApiResponse.success();
	}

}
