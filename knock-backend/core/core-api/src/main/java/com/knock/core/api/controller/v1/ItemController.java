package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
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
	public ApiResponse<Long> createItem(@AuthenticationPrincipal MemberPrincipal principal, @RequestBody ItemCreateRequestDto request) {
		ItemCreateResult result = itemService.createItem(principal.getMemberId(), request.groupId(), ItemCreateData.of(request));
		return ApiResponse.success(result.id());
	}

	@GetMapping("/api/v1/items/{itemId}")
	public ApiResponse<ItemReadResult> getItem(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long itemId) {
		itemService.increaseViewCount(itemId, principal.getMemberId());
		return ApiResponse.success(itemService.getItem(itemId));
	}

	@GetMapping("/api/v1/groups/{groupId}/items")
	public ApiResponse<List<ItemListResult>> getItemsByGroup(@PathVariable Long groupId) {
		return ApiResponse.success(itemService.getItemsByGroup(groupId));
	}

	@GetMapping("/api/v1/items/my-selling")
	public ApiResponse<List<ItemListResult>> getMySelling(@AuthenticationPrincipal MemberPrincipal principal) {
		return ApiResponse.success(itemService.getMySellingItems(principal.getMemberId()));
	}

}
