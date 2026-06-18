package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningResult;

import java.util.List;

public record ItemPolicyWarningResponseDto(String policyVersion, List<String> warningCategories, String policyUrl,
		String severity, String message) {

	public static ItemPolicyWarningResponseDto from(ItemPolicyWarningResult result) {
		return new ItemPolicyWarningResponseDto(result.policyVersion(), result.warningCategories(), result.policyUrl(),
				result.severity(), result.message());
	}

}
