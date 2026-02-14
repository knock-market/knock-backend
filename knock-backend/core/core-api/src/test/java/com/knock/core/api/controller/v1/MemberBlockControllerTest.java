package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.member.MemberService;
import com.knock.core.domain.member.dto.BlockedMemberResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class MemberBlockControllerTest extends RestDocsTest {

	private MemberService memberService;

	private MemberBlockController memberBlockController;

	private MemberPrincipal principal;

	@BeforeEach
	void setUp() {
		memberService = mock(MemberService.class);
		memberBlockController = new MemberBlockController(memberService);
		principal = new MemberPrincipal(TEST_MEMBER_ID, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(memberBlockController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("차단 유저 목록 조회 성공")
	void getBlockedMembers_success() {
		// given
		BlockedMemberResult blockedMember = new BlockedMemberResult(TEST_MEMBER_ID_2, TEST_NAME,
				LocalDateTime.now());
		given(memberService.getBlockedMembers(any())).willReturn(List.of(blockedMember));

		// when & then
		restDocGiven().get("/api/v1/members/my/blocked")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/members/blocked/list", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("차단된 회원 ID"),
							fieldWithPath("data[].name").type(JsonFieldType.STRING).description("차단된 회원 이름"),
							fieldWithPath("data[].blockedAt").description("차단 시각"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("유저 차단 성공")
	void blockMember_success() {
		// given
		doNothing().when(memberService).blockMember(any(), any());

		// when & then
		restDocGiven().pathParam("memberId", TEST_MEMBER_ID_2)
			.post("/api/v1/members/{memberId}/block")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/members/blocked/block", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("memberId").description("차단할 회원 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("유저 차단 해제 성공")
	void unblockMember_success() {
		// given
		doNothing().when(memberService).unblockMember(any(), any());

		// when & then
		restDocGiven().pathParam("memberId", TEST_MEMBER_ID_2)
			.delete("/api/v1/members/{memberId}/block")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/members/blocked/unblock", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("memberId").description("차단 해제할 회원 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
