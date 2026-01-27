package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.bookmark.BookmarkService;
import com.knock.core.domain.bookmark.dto.BookmarkResult;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import com.knock.test.api.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class BookmarkControllerTest extends RestDocsTest {

	private BookmarkService bookmarkService;

	private BookmarkController bookmarkController;

	private MemberPrincipal principal;

	@BeforeEach
	public void setUp() {
		bookmarkService = mock(BookmarkService.class);
		bookmarkController = new BookmarkController(bookmarkService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(bookmarkController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("북마크 토글 성공")
	void toggleBookmark_success() {
		// given
		given(bookmarkService.toggleBookmark(anyLong(), any())).willReturn(true);

		// when & then
		restDocGiven().pathParam("itemId", TEST_ITEM_ID)
			.post("/api/v1/items/{itemId}/bookmarks")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/bookmarks-toggle", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("itemId").description("상품 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.itemId").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data.toggleOn").type(JsonFieldType.BOOLEAN)
								.description("북마크 설정 여부 (true: 설정, false: 해제)"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("내 북마크 목록 조회 성공")
	void getMyBookmarks_success() {
		// given
		Member member = createMember(TEST_MEMBER_ID);
		Item item = createItem(TEST_ITEM_ID, createGroup(), member);
		Bookmark bookmark = createBookmark(TEST_BOOKMARK_ID, member, item);
		BookmarkResult result = BookmarkResult.from(bookmark, TEST_IMAGE_URL);

		given(bookmarkService.getMyBookmarks(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven().get("/api/v1/items/my-bookmarks")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/items/my-bookmarks", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].bookmarkId").type(JsonFieldType.NUMBER).description("북마크 ID"),
							fieldWithPath("data[].itemId").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].title").type(JsonFieldType.STRING).description("상품 제목"),
							fieldWithPath("data[].price").type(JsonFieldType.NUMBER).description("상품 가격"),
							fieldWithPath("data[].thumbnailUrl").type(JsonFieldType.STRING).description("상품 썸네일 URL"),
							fieldWithPath("data[].createdAt").description("생성일"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
