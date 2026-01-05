package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
import com.knock.core.api.controller.v1.response.ItemCreateResponseDto;
import com.knock.core.domain.item.ItemService;
import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemReadResult;
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
public class ItemController {

	private final ItemService itemService;

	@PostMapping("/api/v1/items")
	public ApiResponse<Long> createItem(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody ItemCreateRequestDto request) {
		ItemCreateResult result = itemService.createItem(principal.getMemberId(), request.groupId(),
				ItemCreateData.of(request));
		return ApiResponse.success(result.id());
	}

	@GetMapping("/api/v1/items/{itemId}")
	public ApiResponse<ItemReadResult> getItem(@PathVariable Long itemId) {
		return ApiResponse.success(itemService.getItem(itemId));
	}

	@GetMapping("/api/v1/groups/{groupId}/items")
	public ApiResponse<java.util.List<com.knock.core.domain.item.dto.ItemListResult>> getItemsByGroup(
			@PathVariable Long groupId) {
		return ApiResponse.success(itemService.getItemsByGroup(groupId));
	}

}
