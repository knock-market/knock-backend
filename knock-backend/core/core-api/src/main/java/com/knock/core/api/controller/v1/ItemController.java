package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
import com.knock.core.api.controller.v1.response.ItemResponseDto;
import com.knock.core.api.controller.v1.response.ItemSummaryResponseDto;
import com.knock.core.domain.item.ItemService;
import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@PostMapping("/api/v1/items")
	public ApiResponse<ItemIdResponseDto> createItem(@AuthenticationPrincipal MemberPrincipal principal,
			@Valid @RequestBody ItemCreateRequestDto request) {
		ItemCreateData data = new ItemCreateData(request.title(), request.description(), request.price(),
				request.itemType(), request.imageUrls(), request.tradeLocationName(), request.tradeLocationAddress(),
				request.tradeLatitude(), request.tradeLongitude());
		ItemCreateResult result = itemService.createItem(principal.getMemberId(), data);
		return ApiResponse.success(new ItemIdResponseDto(result.id(), result.publicId()));
	}

	@GetMapping("/api/v1/items/{itemPublicId}")
	public ApiResponse<ItemResponseDto> getItem(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable String itemPublicId, HttpServletRequest request) {
		ItemReadResult result = itemService.getItemByPublicId(itemPublicId);
		Long viewerMemberId = principal == null ? null : principal.getMemberId();
		String viewerSessionId = principal == null ? request.getSession(true).getId() : null;
		itemService.increaseViewCount(result.id(), result.writerId(), viewerMemberId, viewerSessionId);
		return ApiResponse.success(ItemResponseDto.from(result));
	}

	@GetMapping("/api/v1/items/manage/{itemId}")
	public ApiResponse<ItemResponseDto> getItemForManagement(@AuthenticationPrincipal MemberPrincipal principal,
			@PathVariable Long itemId) {
		ItemReadResult result = itemService.getItem(itemId);
		return ApiResponse.success(ItemResponseDto.from(result));
	}

	@GetMapping("/api/v1/items")
	public ApiResponse<List<ItemSummaryResponseDto>> getMarketplaceItems() {
		List<ItemListResult> results = itemService.getMarketplaceItems();
		List<ItemSummaryResponseDto> response = results.stream().map(ItemSummaryResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@GetMapping("/api/v1/items/my-selling")
	public ApiResponse<List<ItemSummaryResponseDto>> getMySelling(@AuthenticationPrincipal MemberPrincipal principal) {
		List<ItemListResult> results = itemService.getMySellingItems(principal.getMemberId());
		List<ItemSummaryResponseDto> response = results.stream().map(ItemSummaryResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@GetMapping("/api/v1/members/{memberId}/items")
	public ApiResponse<List<ItemSummaryResponseDto>> getSellingItemsByMember(@PathVariable Long memberId) {
		List<ItemListResult> results = itemService.getSellingItemsByMember(memberId);
		List<ItemSummaryResponseDto> response = results.stream().map(ItemSummaryResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@DeleteMapping("/api/v1/items/{itemId}")
	public ApiResponse<?> deleteItem(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long itemId) {
		itemService.deleteItem(principal.getMemberId(), itemId);
		return ApiResponse.success();
	}

	public record ItemIdResponseDto(Long id, String publicId) {
	}

}
