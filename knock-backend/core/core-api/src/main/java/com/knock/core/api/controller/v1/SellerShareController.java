package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.SellerShareLinkCreateRequestDto;
import com.knock.core.api.controller.v1.response.SellerShareLinkResponseDto;
import com.knock.core.api.controller.v1.response.SellerShopResponseDto;
import com.knock.core.domain.seller.SellerShareService;
import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SellerShareController {

	private final SellerShareService sellerShareService;

	@PostMapping("/api/v1/seller-shares")
	public ApiResponse<SellerShareLinkResponseDto> createShareLink(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody SellerShareLinkCreateRequestDto request) {
		SellerShareLinkCreateResult result = sellerShareService.createShareLink(principal.getMemberId(),
				request.duration());
		return ApiResponse.success(SellerShareLinkResponseDto.from(result));
	}

	@GetMapping("/api/v1/seller-shares/{token}")
	public ApiResponse<SellerShopResponseDto> getSellerShop(@PathVariable String token) {
		SellerShopResult result = sellerShareService.getSellerShop(token);
		return ApiResponse.success(SellerShopResponseDto.from(result));
	}

}
