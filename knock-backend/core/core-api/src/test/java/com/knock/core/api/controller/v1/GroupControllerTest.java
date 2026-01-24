package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.GroupCreateRequestDto;
import com.knock.core.api.controller.v1.request.GroupJoinRequestDto;
import com.knock.core.domain.group.GroupService;
import com.knock.core.domain.group.dto.GroupResult;
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

class GroupControllerTest extends RestDocsTest {

	private GroupService groupService;

	private GroupController groupController;

	private MemberPrincipal principal;

	@BeforeEach
	public void setUp() {
		groupService = mock(GroupService.class);
		groupController = new GroupController(groupService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(groupController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("그룹 생성 성공")
	void createGroup_success() {
		// given
		GroupCreateRequestDto request = new GroupCreateRequestDto(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION);
		given(groupService.createGroup(anyLong(), any())).willReturn(TEST_GROUP_ID);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
				.body(request)
				.post("/api/v1/groups")
				.then()
				.status(HttpStatus.OK)
				.apply(document("api/v1/groups/create", requestPreprocessor(), responsePreprocessor(),
						requestFields(
								fieldWithPath("name").type(JsonFieldType.STRING).description("그룹 이름"),
								fieldWithPath("description").type(JsonFieldType.STRING).description("그룹 설명")),
						relaxedResponseFields(
								fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
								fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("생성된 그룹 ID"),
								fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("그룹 가입 성공")
	void joinGroup_success() {
		// given
		GroupJoinRequestDto request = new GroupJoinRequestDto(TEST_INVITE_CODE);
		given(groupService.joinGroup(anyLong(), any())).willReturn(TEST_GROUP_ID);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
				.body(request)
				.post("/api/v1/groups/join")
				.then()
				.status(HttpStatus.OK)
				.apply(document("api/v1/groups/join", requestPreprocessor(), responsePreprocessor(),
						requestFields(
								fieldWithPath("inviteCode").type(JsonFieldType.STRING).description("초대 코드")),
						relaxedResponseFields(
								fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
								fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("가입된 그룹 ID"),
								fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("내 그룹 목록 조회 성공")
	void getMyGroups_success() {
		// given
		GroupResult result = new GroupResult(TEST_GROUP_ID, TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, TEST_INVITE_CODE,
				TEST_MEMBER_ID);
		given(groupService.getMyGroups(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven()
				.get("/api/v1/groups/my")
				.then()
				.status(HttpStatus.OK)
				.apply(document("api/v1/groups/my", requestPreprocessor(), responsePreprocessor(),
						relaxedResponseFields(
								fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
								fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("그룹 ID"),
								fieldWithPath("data[].name").type(JsonFieldType.STRING).description("그룹 이름"),
								fieldWithPath("data[].description").type(JsonFieldType.STRING).description("그룹 설명"),
								fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}
}
