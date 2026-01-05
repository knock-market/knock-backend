package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.MemberSignupRequestDto;
import com.knock.core.api.controller.v1.response.MemberResponseDto;
import com.knock.core.api.controller.v1.response.MemberSignupResponseDto;
import com.knock.core.domain.member.MemberService;
import com.knock.core.domain.member.dto.MemberResult;
import com.knock.core.domain.member.dto.MemberSignupData;
import com.knock.core.domain.member.dto.MemberSignupResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/api/v1/members")
	public ApiResponse<MemberSignupResponseDto> signUpMember(@RequestBody MemberSignupRequestDto request) {
		MemberSignupResult result = memberService.signup(MemberSignupData.of(request));
		MemberSignupResponseDto response = MemberSignupResponseDto.of(result);
		return ApiResponse.success(response);
	}

	@GetMapping("/api/v1/members/my")
	public ApiResponse<MemberResponseDto> getMyMember(@AuthenticationPrincipal MemberPrincipal principal) {
		MemberResult result = memberService.getMember(principal.getMemberId());
		MemberResponseDto response = MemberResponseDto.of(result);
		return ApiResponse.success(response);
	}

}
