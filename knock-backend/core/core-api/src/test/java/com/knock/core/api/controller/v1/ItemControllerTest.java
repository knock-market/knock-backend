package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
import com.knock.core.domain.item.ItemService;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class ItemControllerTest extends RestDocsTest {

	private ItemService itemService;

	private ItemController itemController;

	private MemberPrincipal principal;

	@BeforeEach
	public void setUp() {
		itemService = mock(ItemService.class);
		itemController = new ItemController(itemService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(itemController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("상품 등록 성공")
	void createItem_success() {
		// given
		ItemCreateRequestDto request = new ItemCreateRequestDto(TEST_GROUP_ID, TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION,
				TEST_ITEM_PRICE, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, List.of(TEST_IMAGE_URL));
		given(itemService.createItem(anyLong(), anyLong(), any())).willReturn(new ItemCreateResult(TEST_ITEM_ID));

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/items")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/create", requestPreprocessor(), responsePreprocessor(),
					relaxedRequestFields(fieldWithPath("groupId").type(JsonFieldType.NUMBER).description("그룹 ID"),
							fieldWithPath("title").type(JsonFieldType.STRING).description("상품 제목"),
							fieldWithPath("description").type(JsonFieldType.STRING).description("상품 설명"),
							fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("itemType").type(JsonFieldType.STRING).description("거래 유형 (SELL, BUY)"),
							fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
							fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("생성된 상품 ID"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("상품 상세 조회 성공")
	void getItem_success() {
		// given
		ItemReadResult result = new ItemReadResult(TEST_ITEM_ID, TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION,
				TEST_ITEM_PRICE, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, ItemStatus.ON_SALE,
				List.of(TEST_IMAGE_URL), TEST_MEMBER_ID);
		given(itemService.getItem(anyLong())).willReturn(result);

		// when & then
		restDocGiven().pathParam("itemId", TEST_ITEM_ID)
			.get("/api/v1/items/{itemId}")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/get", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("itemId").description("상품 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data.description").type(JsonFieldType.STRING).description("설명"),
							fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("data.type").type(JsonFieldType.STRING).description("거래 유형"),
							fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리"),
							fieldWithPath("data.status").type(JsonFieldType.STRING).description("상품 상태"),
							fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록"),
							fieldWithPath("data.writerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("그룹별 상품 목록 조회 성공")
	void getItemsByGroup_success() {
		// given
		ItemListResult result = new ItemListResult(TEST_ITEM_ID, TEST_ITEM_TITLE, TEST_ITEM_PRICE, ItemType.SELL,
				ItemCategory.DIGITAL_DEVICE, ItemStatus.ON_SALE, TEST_IMAGE_URL, TEST_MEMBER_ID);
		given(itemService.getItemsByGroup(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven().pathParam("groupId", TEST_GROUP_ID)
			.get("/api/v1/groups/{groupId}/items")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/list-by-group", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("groupId").description("그룹 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data[].price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("data[].type").type(JsonFieldType.STRING).description("거래 유형"),
							fieldWithPath("data[].category").type(JsonFieldType.STRING).description("카테고리"),
							fieldWithPath("data[].status").type(JsonFieldType.STRING).description("상품 상태"),
							fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 URL"),
							fieldWithPath("data[].writerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("내 판매 내역 조회 성공")
	void getMySelling_success() {
		// given
		ItemListResult result = new ItemListResult(TEST_ITEM_ID, TEST_ITEM_TITLE, TEST_ITEM_PRICE, ItemType.SELL,
				ItemCategory.DIGITAL_DEVICE, ItemStatus.ON_SALE, TEST_IMAGE_URL, TEST_MEMBER_ID);
		given(itemService.getMySellingItems(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven().get("/api/v1/items/my-selling")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/my-selling", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data[].price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("data[].type").type(JsonFieldType.STRING).description("거래 유형"),
							fieldWithPath("data[].category").type(JsonFieldType.STRING).description("카테고리"),
							fieldWithPath("data[].status").type(JsonFieldType.STRING).description("상품 상태"),
							fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 URL"),
							fieldWithPath("data[].writerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
