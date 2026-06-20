package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ItemListRequestDto;
import com.knock.core.api.controller.v1.request.SellerShareLinkCreateRequestDto;
import com.knock.core.api.controller.v1.response.SellerShareLinkResponseDto;
import com.knock.core.api.controller.v1.response.SellerShareLinkSummaryResponseDto;
import com.knock.core.api.controller.v1.response.SellerShopResponseDto;
import com.knock.core.domain.seller.SellerShareService;
import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShareLinkStatsResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
	public ApiResponse<SellerShopResponseDto> getSellerShop(@PathVariable String token,
			@ModelAttribute ItemListRequestDto request) {
		SellerShopResult result = sellerShareService.getSellerShop(token, request.toQuery());
		return ApiResponse.success(SellerShopResponseDto.from(result));
	}

	@GetMapping("/api/v1/seller-shares/my")
	public ApiResponse<List<SellerShareLinkSummaryResponseDto>> getMyShareLinks(
			@AuthenticationPrincipal MemberPrincipal principal) {
		List<SellerShareLinkStatsResult> results = sellerShareService.getMyShareLinks(principal.getMemberId());
		return ApiResponse.success(results.stream().map(SellerShareLinkSummaryResponseDto::from).toList());
	}

	@DeleteMapping("/api/v1/seller-shares/{token}")
	public ApiResponse<?> deactivateShareLink(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable String token) {
		sellerShareService.deactivateShareLink(principal.getMemberId(), token);
		return ApiResponse.success();
	}

}
