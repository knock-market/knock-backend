package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.item.ItemService;
import com.knock.core.domain.item.dto.ItemListResult;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

class ItemListingControllerTest extends RestDocsTest {

	private ItemService itemService;

	@BeforeEach
	public void setUp() {
		itemService = mock(ItemService.class);
		ItemController itemController = new ItemController(itemService);
		MemberPrincipal principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(itemController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("공개 상품 목록 조회 성공 - 검색 쿼리")
	void getMarketplaceItems_success() {
		// given
		ItemListResult result = itemListResult();
		given(itemService.getMarketplaceItems(any())).willReturn(List.of(result));

		// when & then
		restDocGiven().queryParam("keyword", "테스트")
			.queryParam("location", "강남")
			.queryParam("status", "ON_SALE")
			.queryParam("sort", "LATEST")
			.queryParam("page", 0)
			.queryParam("size", 20)
			.get("/api/v1/items")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/list", requestPreprocessor(), responsePreprocessor(),
					queryParameters(listingQueryParameters()),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].publicId").type(JsonFieldType.STRING).description("공개 상품 식별자"),
							fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data[].likesCount").type(JsonFieldType.NUMBER).description("관심 수"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("내 판매 내역 조회 성공")
	void getMySelling_success() {
		// given
		ItemListResult result = itemListResult();
		given(itemService.getMySellingItems(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven().get("/api/v1/items/my-selling")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/my-selling", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].publicId").type(JsonFieldType.STRING).description("공개 상품 식별자"),
							fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data[].price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("data[].type").type(JsonFieldType.STRING).description("거래 유형"),
							fieldWithPath("data[].status").type(JsonFieldType.STRING).description("상품 상태"),
							fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 URL"),
							fieldWithPath("data[].writerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("data[].writerNickname").type(JsonFieldType.STRING).description("판매자 닉네임"),
							fieldWithPath("data[].writerProfileImageUrl").type(JsonFieldType.STRING)
								.description("판매자 프로필 이미지 URL"),
							fieldWithPath("data[].likesCount").type(JsonFieldType.NUMBER).description("관심 수"),
							fieldWithPath("data[].viewCount").type(JsonFieldType.NUMBER).description("조회 수"),
							fieldWithPath("data[].postedAt").type(JsonFieldType.STRING).description("등록 일시"),
							fieldWithPath("data[].tradeLocationName").type(JsonFieldType.STRING)
								.description("거래 위치 이름"),
							fieldWithPath("data[].tradeLocationAddress").type(JsonFieldType.STRING)
								.description("거래 위치 주소"),
							fieldWithPath("data[].tradeLatitude").type(JsonFieldType.NUMBER).description("거래 위치 위도"),
							fieldWithPath("data[].tradeLongitude").type(JsonFieldType.NUMBER).description("거래 위치 경도"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("회원 판매 상품 조회 성공")
	void getSellingItemsByMember_success() {
		// given
		ItemListResult result = itemListResult();
		given(itemService.getSellingItemsByMember(eq(TEST_MEMBER_ID), any())).willReturn(List.of(result));

		// when & then
		restDocGiven().pathParam("memberId", TEST_MEMBER_ID)
			.queryParam("keyword", "테스트")
			.queryParam("location", "강남")
			.queryParam("status", "ON_SALE")
			.queryParam("sort", "POPULAR")
			.queryParam("page", 0)
			.queryParam("size", 20)
			.get("/api/v1/members/{memberId}/items")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/list-by-member", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("memberId").description("회원 ID")),
					queryParameters(listingQueryParameters()),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].publicId").type(JsonFieldType.STRING).description("공개 상품 식별자"),
							fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data[].price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("data[].type").type(JsonFieldType.STRING).description("거래 유형"),
							fieldWithPath("data[].status").type(JsonFieldType.STRING).description("상품 상태"),
							fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 URL"),
							fieldWithPath("data[].writerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("data[].writerNickname").type(JsonFieldType.STRING).description("판매자 닉네임"),
							fieldWithPath("data[].writerProfileImageUrl").type(JsonFieldType.STRING)
								.description("판매자 프로필 이미지 URL"),
							fieldWithPath("data[].likesCount").type(JsonFieldType.NUMBER).description("관심 수"),
							fieldWithPath("data[].postedAt").type(JsonFieldType.STRING).description("등록 일시"),
							fieldWithPath("data[].tradeLocationName").type(JsonFieldType.STRING)
								.description("거래 위치 이름"),
							fieldWithPath("data[].tradeLocationAddress").type(JsonFieldType.STRING)
								.description("거래 위치 주소"),
							fieldWithPath("data[].tradeLatitude").type(JsonFieldType.NUMBER).description("거래 위치 위도"),
							fieldWithPath("data[].tradeLongitude").type(JsonFieldType.NUMBER).description("거래 위치 경도"),
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
