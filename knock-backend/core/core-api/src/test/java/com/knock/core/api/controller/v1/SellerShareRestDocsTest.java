package com.knock.core.api.controller.v1;

import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.seller.SellerShareService;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.test.api.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

class SellerShareRestDocsTest extends RestDocsTest {

	private SellerShareService sellerShareService;

	@BeforeEach
	public void setUp() {
		sellerShareService = mock(SellerShareService.class);
		SellerShareController sellerShareController = new SellerShareController(sellerShareService);
		mockMvc = mockController(sellerShareController, new ApiControllerAdvice());
	}

	@Test
	@DisplayName("판매자 공유 페이지 조회 성공 - 검색 쿼리")
	void getSellerShop_success() {
		// given
		SellerShopResult result = new SellerShopResult(TEST_MEMBER_ID, TEST_NAME, TEST_NICKNAME, TEST_IMAGE_URL,
				List.of(itemListResult()));
		given(sellerShareService.getSellerShop(eq(TEST_SELLER_SHARE_TOKEN), any())).willReturn(result);

		// when & then
		restDocGiven().pathParam("token", TEST_SELLER_SHARE_TOKEN)
			.queryParam("keyword", "테스트")
			.queryParam("location", "강남")
			.queryParam("status", "ON_SALE")
			.queryParam("sort", "POPULAR")
			.queryParam("page", 0)
			.queryParam("size", 20)
			.get("/api/v1/seller-shares/{token}")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/seller-shares/get-shop", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("token").description("판매자 공유 토큰")),
					queryParameters(listingQueryParameters()),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.sellerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("data.sellerName").type(JsonFieldType.STRING).description("판매자 이름"),
							fieldWithPath("data.sellerNickname").type(JsonFieldType.STRING).description("판매자 닉네임"),
							fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data.items[].publicId").type(JsonFieldType.STRING).description("공개 상품 식별자"),
							fieldWithPath("data.items[].likesCount").type(JsonFieldType.NUMBER).description("관심 수"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	private ItemListResult itemListResult() {
		return new ItemListResult(TEST_ITEM_ID, TEST_ITEM_PUBLIC_ID, TEST_ITEM_TITLE, TEST_ITEM_PRICE, ItemType.SELL,
				ItemStatus.ON_SALE, TEST_IMAGE_URL, TEST_MEMBER_ID, TEST_NICKNAME, TEST_IMAGE_URL, 0L,
				java.time.LocalDateTime.now(), TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
				TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
	}

	private ParameterDescriptor[] listingQueryParameters() {
		return new ParameterDescriptor[] { parameterWithName("keyword").optional().description("제목/설명 검색어"),
				parameterWithName("location").optional().description("거래 위치 검색어"),
				parameterWithName("status").optional().description("상품 상태. 기본 ON_SALE"),
				parameterWithName("sort").optional().description("LATEST, POPULAR, PRICE_ASC, PRICE_DESC"),
				parameterWithName("page").optional().description("0-base 페이지. 기본 0"),
				parameterWithName("size").optional().description("페이지 크기. 기본 20, 최대 50") };
	}

}
