package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
import com.knock.core.domain.item.ItemService;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemReadResult;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
		ItemCreateRequestDto request = new ItemCreateRequestDto(TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
				ItemType.SELL, List.of(TEST_IMAGE_URL), TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
				TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
		given(itemService.createItem(anyLong(), any()))
			.willReturn(new ItemCreateResult(TEST_ITEM_ID, TEST_ITEM_PUBLIC_ID));

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/items")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/create", requestPreprocessor(), responsePreprocessor(),
					relaxedRequestFields(fieldWithPath("title").type(JsonFieldType.STRING).description("상품 제목"),
							fieldWithPath("description").type(JsonFieldType.STRING).description("상품 설명"),
							fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("itemType").type(JsonFieldType.STRING).description("거래 유형 (SELL, BUY)"),
							fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록"),
							fieldWithPath("tradeLocationName").type(JsonFieldType.STRING).description("거래 위치 이름"),
							fieldWithPath("tradeLocationAddress").type(JsonFieldType.STRING).description("거래 위치 주소"),
							fieldWithPath("tradeLatitude").type(JsonFieldType.NUMBER).description("거래 위치 위도"),
							fieldWithPath("tradeLongitude").type(JsonFieldType.NUMBER).description("거래 위치 경도")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("생성된 상품 ID"),
							fieldWithPath("data.publicId").type(JsonFieldType.STRING).description("공개 상품 식별자"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("상품 등록 실패 - 유효성 검증")
	void createItem_fail_validation() {
		// given
		ItemCreateRequestDto request = new ItemCreateRequestDto("", TEST_ITEM_DESCRIPTION, -1L, null,
				List.of(TEST_IMAGE_URL), "", TEST_TRADE_LOCATION_ADDRESS, 91.0, TEST_TRADE_LONGITUDE);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/items")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(itemService);
	}

	@Test
	@DisplayName("상품 상세 조회 성공")
	void getItem_success() {
		// given
		ItemReadResult result = new ItemReadResult(TEST_ITEM_ID, TEST_ITEM_PUBLIC_ID, TEST_ITEM_TITLE,
				TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE, ItemType.SELL, ItemStatus.ON_SALE, List.of(TEST_IMAGE_URL),
				TEST_MEMBER_ID, TEST_NICKNAME, TEST_IMAGE_URL, TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
				TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
		given(itemService.getItemByPublicId(principal.getMemberId(), TEST_ITEM_PUBLIC_ID)).willReturn(result);

		// when & then
		restDocGiven().pathParam("itemPublicId", TEST_ITEM_PUBLIC_ID)
			.get("/api/v1/items/{itemPublicId}")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/get", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("itemPublicId").description("공개 상품 식별자")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data.publicId").type(JsonFieldType.STRING).description("공개 상품 식별자"),
							fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
							fieldWithPath("data.description").type(JsonFieldType.STRING).description("설명"),
							fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("가격"),
							fieldWithPath("data.type").type(JsonFieldType.STRING).description("거래 유형"),
							fieldWithPath("data.status").type(JsonFieldType.STRING).description("상품 상태"),
							fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록"),
							fieldWithPath("data.writerId").type(JsonFieldType.NUMBER).description("판매자 ID"),
							fieldWithPath("data.writerNickname").type(JsonFieldType.STRING).description("판매자 닉네임"),
							fieldWithPath("data.writerProfileImageUrl").type(JsonFieldType.STRING)
								.description("판매자 프로필 이미지 URL"),
							fieldWithPath("data.tradeLocationName").type(JsonFieldType.STRING).description("거래 위치 이름"),
							fieldWithPath("data.tradeLocationAddress").type(JsonFieldType.STRING)
								.description("거래 위치 주소"),
							fieldWithPath("data.tradeLatitude").type(JsonFieldType.NUMBER).description("거래 위치 위도"),
							fieldWithPath("data.tradeLongitude").type(JsonFieldType.NUMBER).description("거래 위치 경도"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("로그인 상품 상세 조회 시 회원 ID로 조회수를 기록한다")
	void getItem_memberSuccess() {
		// given
		ItemReadResult result = new ItemReadResult(TEST_ITEM_ID, TEST_ITEM_PUBLIC_ID, TEST_ITEM_TITLE,
				TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE, ItemType.SELL, ItemStatus.ON_SALE, List.of(TEST_IMAGE_URL),
				TEST_MEMBER_ID_2, TEST_NICKNAME, TEST_IMAGE_URL, TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
				TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
		given(itemService.getItemByPublicId(principal.getMemberId(), TEST_ITEM_PUBLIC_ID)).willReturn(result);

		// when
		itemController.getItem(principal, TEST_ITEM_PUBLIC_ID);

		// then
		verify(itemService).increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID_2, principal.getMemberId(), null);
	}

	@Test
	@DisplayName("관리용 상품 상세 조회 시 판매자 본인 권한으로 조회한다")
	void getItemForManagement_ownerScoped() {
		// given
		ItemReadResult result = new ItemReadResult(TEST_ITEM_ID, TEST_ITEM_PUBLIC_ID, TEST_ITEM_TITLE,
				TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE, ItemType.SELL, ItemStatus.ON_SALE, List.of(TEST_IMAGE_URL),
				principal.getMemberId(), TEST_NICKNAME, TEST_IMAGE_URL, TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
				TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
		given(itemService.getItemForOwner(principal.getMemberId(), TEST_ITEM_ID)).willReturn(result);

		// when
		itemController.getItemForManagement(principal, TEST_ITEM_ID);

		// then
		verify(itemService).getItemForOwner(principal.getMemberId(), TEST_ITEM_ID);
	}

	@Test
	@DisplayName("상품 삭제 성공")
	void deleteItem_success() {
		// given
		doNothing().when(itemService).deleteItem(anyLong(), anyLong());

		// when & then
		restDocGiven().pathParam("itemId", TEST_ITEM_ID)
			.delete("/api/v1/items/{itemId}")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/delete", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("itemId").description("상품 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
