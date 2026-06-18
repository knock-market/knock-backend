package com.knock.core.domain.itempolicy.dto;

import java.util.List;

public record ItemPolicyWarningResult(String policyVersion, List<String> warningCategories, String policyUrl,
		String severity, String message) {
}
