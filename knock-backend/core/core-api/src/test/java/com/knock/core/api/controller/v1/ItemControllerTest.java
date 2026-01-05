/*
 * package com.knock.core.api.controller.v1;
 * 
 * import com.knock.core.api.controller.v1.request.ItemCreateRequestDto;
 * import com.knock.core.domain.item.ItemService;
 * import com.knock.core.domain.item.dto.ItemCreateData;
 * import com.knock.core.domain.item.dto.ItemCreateResult;
 * import com.knock.core.domain.item.dto.ItemReadResult;
 * import com.knock.core.enums.ItemCategory;
 * import com.knock.core.enums.ItemStatus;
 * import com.knock.core.enums.ItemType;
 * import com.knock.test.api.RestDocsTest;
 * import org.junit.jupiter.api.BeforeEach;
 * import org.junit.jupiter.api.Test;
 * import org.springframework.http.MediaType;
 * import org.springframework.restdocs.payload.JsonFieldType;
 * 
 * import java.util.List;
 * 
 * import static com.knock.test.api.RestDocsUtils.requestPreprocessor;
 * import static com.knock.test.api.RestDocsUtils.responsePreprocessor;
 * import static org.mockito.ArgumentMatchers.any;
 * import static org.mockito.ArgumentMatchers.eq;
 * import static org.mockito.BDDMockito.given;
 * import static org.mockito.Mockito.mock;
 * import static
 * org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
 * import static
 * org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
 * import static
 * org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
 * import static org.springframework.restdocs.payload.PayloadDocumentation.*;
 * import static
 * org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
 * import static
 * org.springframework.restdocs.request.RequestDocumentation.pathParameters;
 * import static
 * org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 * 
 * class ItemControllerTest extends RestDocsTest {
 * 
 * private ItemService itemService;
 * 
 * private ItemController itemController;
 * 
 * @BeforeEach
 * void setUp() {
 * itemService = mock(ItemService.class);
 * itemController = new ItemController(itemService);
 * mockController(itemController);
 * }
 * 
 * @Test
 * void createItem() throws Exception {
 * given(itemService.createItem(any(), any(), any())).willReturn(new
 * ItemCreateResult(1L));
 * 
 * ItemCreateRequestDto request = new ItemCreateRequestDto(1L, "아이폰 15 프로",
 * "배터리 성능 100% 입니다.", 1500000L,
 * ItemType.SELL, ItemCategory.DIGITAL_DEVICE,
 * List.of("http://example.com/image1.jpg", "http://example.com/image2.jpg"));
 * 
 * mockMvc
 * .perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
 * .content(objectMapper.writeValueAsString(request)))
 * .andExpect(status().isOk())
 * .andDo(document("item-create", requestPreprocessor(), responsePreprocessor(),
 * requestFields(fieldWithPath("groupId").type(JsonFieldType.NUMBER).
 * description("그룹 ID"),
 * fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
 * fieldWithPath("description").type(JsonFieldType.STRING).description("내용"),
 * fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
 * fieldWithPath("itemType").type(JsonFieldType.STRING).
 * description("물건 타입 (SELL, SHARE)"),
 * fieldWithPath("category").type(JsonFieldType.STRING).description("카테고리"),
 * fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록"
 * )),
 * responseFields(fieldWithPath("result").type(JsonFieldType.STRING).
 * description("결과 코드"),
 * fieldWithPath("data").type(JsonFieldType.NUMBER).description("생성된 물건 ID"),
 * fieldWithPath("message").type(JsonFieldType.NULL).ignored(),
 * fieldWithPath("error").type(JsonFieldType.NULL).ignored())));
 * }
 * 
 * @Test
 * void getItem() throws Exception {
 * ItemReadResult result = new ItemReadResult(1L, "아이폰 15 프로",
 * "배터리 성능 100% 입니다.", 1500000L, ItemType.SELL,
 * ItemCategory.DIGITAL_DEVICE, ItemStatus.ON_SALE,
 * List.of("http://example.com/image1.jpg"), 100L);
 * given(itemService.getItem(1L)).willReturn(result);
 * 
 * mockMvc.perform(get("/api/v1/items/{itemId}", 1L))
 * .andExpect(status().isOk())
 * .andDo(document("item-get", requestPreprocessor(), responsePreprocessor(),
 * pathParameters(parameterWithName("itemId").description("물건 ID")),
 * responseFields(fieldWithPath("result").type(JsonFieldType.STRING).
 * description("결과 코드"),
 * fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("물건 ID"),
 * fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
 * fieldWithPath("data.description").type(JsonFieldType.STRING).description("내용"
 * ),
 * fieldWithPath("data.price").type(JsonFieldType.NUMBER).description("가격"),
 * fieldWithPath("data.type").type(JsonFieldType.STRING).description("물건 타입"),
 * fieldWithPath("data.category").type(JsonFieldType.STRING).description("카테고리")
 * ,
 * fieldWithPath("data.status").type(JsonFieldType.STRING).description("상태"),
 * fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).
 * description("이미지 URL 목록"),
 * fieldWithPath("data.writerId").type(JsonFieldType.NUMBER).
 * description("작성자 ID"),
 * fieldWithPath("message").type(JsonFieldType.NULL).ignored(),
 * fieldWithPath("error").type(JsonFieldType.NULL).ignored())));
 * }
 * 
 * }
 */
