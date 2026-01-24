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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@PostMapping("/api/v1/items")
	public ApiResponse<ItemIdResponseDto> createItem(@AuthenticationPrincipal MemberPrincipal principal, @RequestBody ItemCreateRequestDto request) {
		ItemCreateResult result = itemService.createItem(principal.getMemberId(), request.groupId(), ItemCreateData.of(request));
		return ApiResponse.success(new ItemIdResponseDto(result.id()));
	}

	@GetMapping("/api/v1/items/{itemId}")
	public ApiResponse<ItemResponseDto> getItem(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long itemId) {
		itemService.increaseViewCount(itemId, principal.getMemberId());
		ItemReadResult result = itemService.getItem(itemId);
		return ApiResponse.success(ItemResponseDto.from(result));
	}

	@GetMapping("/api/v1/groups/{groupId}/items")
	public ApiResponse<List<ItemSummaryResponseDto>> getItemsByGroup(@PathVariable Long groupId) {
		List<ItemListResult> results = itemService.getItemsByGroup(groupId);
		List<ItemSummaryResponseDto> response = results.stream().map(ItemSummaryResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	@GetMapping("/api/v1/items/my-selling")
	public ApiResponse<List<ItemSummaryResponseDto>> getMySelling(@AuthenticationPrincipal MemberPrincipal principal) {
		List<ItemListResult> results = itemService.getMySellingItems(principal.getMemberId());
		List<ItemSummaryResponseDto> response = results.stream().map(ItemSummaryResponseDto::from).toList();
		return ApiResponse.success(response);
	}

	public record ItemIdResponseDto(Long id) {
	}

}
