package com.knock.core.domain.itempolicy;

import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningData;
import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ItemPolicyWarningService {

	private static final String POLICY_VERSION = "2026-06-18.p0";

	private static final String POLICY_URL = "/docs/marketplace-item-policy";

	private static final String NONE = "NONE";

	private static final String WARNING = "WARNING";

	private static final String SAFE_MESSAGE = "No item policy warning was found. Continue to follow marketplace rules.";

	private static final String WARNING_MESSAGE = "This listing may match marketplace policy warnings. Review it before posting.";

	private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of("PROHIBITED_ITEM",
			List.of("weapon", "drug", "medicine", "alcohol", "tobacco", "adult", "무기", "마약", "의약품", "담배", "주류", "성인용품"),
			"COUNTERFEIT_OR_STOLEN_SUSPECTED",
			List.of("counterfeit", "replica", "stolen", "fake", "위조", "짝퉁", "복제품", "도난"), "OFF_PLATFORM_PAYMENT",
			List.of("wire transfer", "bank transfer only", "outside app", "직거래 송금", "외부 결제"));

	public ItemPolicyWarningResult getWarnings(ItemPolicyWarningData data) {
		String normalized = normalize(data.title() + " " + data.description());
		List<String> categories = CATEGORY_KEYWORDS.entrySet()
			.stream()
			.filter(entry -> containsAny(normalized, entry.getValue()))
			.map(Map.Entry::getKey)
			.sorted()
			.toList();

		String severity = categories.isEmpty() ? NONE : WARNING;
		String message = categories.isEmpty() ? SAFE_MESSAGE : WARNING_MESSAGE;
		return new ItemPolicyWarningResult(POLICY_VERSION, categories, POLICY_URL, severity, message);
	}

	private boolean containsAny(String text, List<String> keywords) {
		return keywords.stream().map(this::normalize).anyMatch(text::contains);
	}

	private String normalize(String value) {
		return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}가-힣]+", " ").trim();
	}

}
