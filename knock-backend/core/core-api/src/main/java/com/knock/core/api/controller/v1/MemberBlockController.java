package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.response.BlockedMemberResponseDto;
import com.knock.core.domain.member.MemberService;
import com.knock.core.domain.member.dto.BlockedMemberResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberBlockController {

	private final MemberService memberService;

	@GetMapping("/api/v1/members/my/blocked")
	public ApiResponse<List<BlockedMemberResponseDto>> getBlockedMembers(
			@AuthenticationPrincipal MemberPrincipal principal) {
		List<BlockedMemberResult> results = memberService.getBlockedMembers(principal.getMemberId());
		List<BlockedMemberResponseDto> response = results.stream().map(BlockedMemberResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@PostMapping("/api/v1/members/{memberId}/block")
	public ApiResponse<?> blockMember(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long memberId) {
		memberService.blockMember(principal.getMemberId(), memberId);
		return ApiResponse.success();
	}

	@DeleteMapping("/api/v1/members/{memberId}/block")
	public ApiResponse<?> unblockMember(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long memberId) {
		memberService.unblockMember(principal.getMemberId(), memberId);
		return ApiResponse.success();
	}

}
