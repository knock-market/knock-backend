package com.knock.core.api.controller.v1;

import com.knock.core.api.controller.v1.request.ItemPolicyWarningRequestDto;
import com.knock.core.api.controller.v1.response.ItemPolicyWarningResponseDto;
import com.knock.core.domain.itempolicy.ItemPolicyWarningService;
import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningData;
import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningResult;
import com.knock.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItemPolicyController {

	private final ItemPolicyWarningService itemPolicyWarningService;

	@PostMapping("/api/v1/item-policy/warnings")
	public ApiResponse<ItemPolicyWarningResponseDto> getWarnings(
			@Valid @RequestBody ItemPolicyWarningRequestDto request) {
		ItemPolicyWarningData data = new ItemPolicyWarningData(request.title(), request.description(),
				request.itemType());
		ItemPolicyWarningResult result = itemPolicyWarningService.getWarnings(data);
		return ApiResponse.success(ItemPolicyWarningResponseDto.from(result));
	}

}
