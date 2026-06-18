package com.knock.core.api.controller.v1;

import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.ItemPolicyWarningRequestDto;
import com.knock.core.domain.itempolicy.ItemPolicyWarningService;
import com.knock.core.domain.itempolicy.dto.ItemPolicyWarningResult;
import com.knock.core.enums.ItemType;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.knock.test.api.RestDocsUtils.requestPreprocessor;
import static com.knock.test.api.RestDocsUtils.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;

class ItemPolicyControllerTest extends RestDocsTest {

	private ItemPolicyWarningService itemPolicyWarningService;

	@BeforeEach
	void setUp() {
		itemPolicyWarningService = mock(ItemPolicyWarningService.class);
		ItemPolicyController controller = new ItemPolicyController(itemPolicyWarningService);
		mockMvc = mockController(controller, new ApiControllerAdvice());
	}

	@Test
	@DisplayName("상품 정책 warning preflight 성공")
	void getWarnings_success() {
		ItemPolicyWarningRequestDto request = new ItemPolicyWarningRequestDto("Replica luxury handbag",
				"counterfeit suspected", ItemType.SELL);
		given(itemPolicyWarningService.getWarnings(any())).willReturn(new ItemPolicyWarningResult("2026-06-18.p0",
				List.of("COUNTERFEIT_OR_STOLEN_SUSPECTED"), "/docs/marketplace-item-policy", "WARNING",
				"This listing may match marketplace policy warnings. Review it before posting."));

		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/item-policy/warnings")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/item-policy/warnings", requestPreprocessor(), responsePreprocessor(),
					relaxedRequestFields(fieldWithPath("title").type(JsonFieldType.STRING).description("상품 제목"),
							fieldWithPath("description").type(JsonFieldType.STRING).description("상품 설명"),
							fieldWithPath("itemType").type(JsonFieldType.STRING).description("상품 타입: SELL 또는 GIVE")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.policyVersion").type(JsonFieldType.STRING).description("정책 버전"),
							fieldWithPath("data.warningCategories").type(JsonFieldType.ARRAY)
								.description("warning 카테고리 목록"),
							fieldWithPath("data.policyUrl").type(JsonFieldType.STRING).description("정책 안내 URL"),
							fieldWithPath("data.severity").type(JsonFieldType.STRING)
								.description("MVP severity: NONE 또는 WARNING"),
							fieldWithPath("data.message").type(JsonFieldType.STRING).description("사용자 안내 문구"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("상품 정책 warning preflight 실패 - 제목 검증")
	void getWarnings_failValidation() {
		ItemPolicyWarningRequestDto request = new ItemPolicyWarningRequestDto("", "description", ItemType.SELL);

		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/item-policy/warnings")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(itemPolicyWarningService);
	}

}
