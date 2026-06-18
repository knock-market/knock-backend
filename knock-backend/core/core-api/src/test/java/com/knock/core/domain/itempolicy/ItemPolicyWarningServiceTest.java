package com.knock.core.domain.itempolicy;

import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningData;
import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningResult;
import com.knock.core.enums.ItemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemPolicyWarningServiceTest {

	private final ItemPolicyWarningService service = new ItemPolicyWarningService();

	@Test
	@DisplayName("금지 품목 의심 키워드는 WARNING을 반환한다")
	void getWarnings_warningKeyword() {
		ItemPolicyWarningData data = new ItemPolicyWarningData("Replica luxury handbag",
				"Counterfeit or stolen item suspected", ItemType.SELL);

		ItemPolicyWarningResult result = service.getWarnings(data);

		assertThat(result.severity()).isEqualTo("WARNING");
		assertThat(result.warningCategories()).contains("COUNTERFEIT_OR_STOLEN_SUSPECTED");
		assertThat(result.policyVersion()).isNotBlank();
		assertThat(result.policyUrl()).isNotBlank();
	}

	@Test
	@DisplayName("정상 상품은 NONE을 반환한다")
	void getWarnings_noKeyword() {
		ItemPolicyWarningData data = new ItemPolicyWarningData("Wooden desk", "Clean desk for pickup", ItemType.GIVE);

		ItemPolicyWarningResult result = service.getWarnings(data);

		assertThat(result.severity()).isEqualTo("NONE");
		assertThat(result.warningCategories()).isEmpty();
	}

}
