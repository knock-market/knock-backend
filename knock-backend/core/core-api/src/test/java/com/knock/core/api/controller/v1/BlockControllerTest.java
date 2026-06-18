package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.block.BlockService;
import com.knock.core.domain.block.dto.BlockResult;
import com.knock.test.api.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;
import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class BlockControllerTest extends RestDocsTest {

	private BlockService blockService;

	private BlockController blockController;

	private MemberPrincipal principal;

	@BeforeEach
	void setUp() {
		blockService = mock(BlockService.class);
		blockController = new BlockController(blockService);
		principal = new MemberPrincipal(TEST_MEMBER_ID, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(blockController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("회원 차단 성공")
	void blockMember_success() {
		given(blockService.blockMember(anyLong(), anyLong()))
			.willReturn(new BlockResult(TEST_MEMBER_ID_2, TEST_NICKNAME, null, LocalDateTime.of(2026, 6, 18, 12, 0)));

		restDocGiven().pathParam("memberId", TEST_MEMBER_ID_2)
			.post("/api/v1/blocks/{memberId}")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/blocks/create", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("memberId").description("차단할 회원 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("차단된 회원 ID"),
							fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("차단된 회원 닉네임"),
							fieldWithPath("data.profileImageUrl").type(JsonFieldType.NULL)
								.description("차단된 회원 프로필 이미지 URL"),
							fieldWithPath("data.blockedAt").description("차단 시각"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("회원 차단 해제 성공")
	void unblockMember_success() {
		restDocGiven().pathParam("memberId", TEST_MEMBER_ID_2)
			.delete("/api/v1/blocks/{memberId}")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/blocks/delete", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("memberId").description("차단 해제할 회원 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("내 차단 목록 조회 성공")
	void getMyBlocks_success() {
		given(blockService.getMyBlocks(anyLong()))
			.willReturn(List.of(new BlockResult(TEST_MEMBER_ID_2, TEST_NICKNAME, null,
					LocalDateTime.of(2026, 6, 18, 12, 0))));

		restDocGiven().get("/api/v1/blocks/my")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/blocks/my-list", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER).description("차단된 회원 ID"),
							fieldWithPath("data[].nickname").type(JsonFieldType.STRING).description("차단된 회원 닉네임"),
							fieldWithPath("data[].profileImageUrl").type(JsonFieldType.NULL)
								.description("차단된 회원 프로필 이미지 URL"),
							fieldWithPath("data[].blockedAt").description("차단 시각"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
