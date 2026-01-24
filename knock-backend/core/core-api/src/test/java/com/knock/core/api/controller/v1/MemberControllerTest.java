package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.MemberSignupRequestDto;
import com.knock.core.domain.member.MemberService;
import com.knock.core.domain.member.dto.MemberResult;
import com.knock.core.domain.member.dto.MemberSignupResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.knock.core.support.TestConstants.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

class MemberControllerTest extends RestDocsTest {

    private MemberService memberService;

    private MemberController memberController;

    private MemberPrincipal principal;

    @BeforeEach
    public void setUp() {
        memberService = mock(MemberService.class);
        memberController = new MemberController(memberService);
        principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
        mockMvc = mockController(memberController, new ApiControllerAdvice(), principalResolver(principal));
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        MemberSignupRequestDto request = new MemberSignupRequestDto(TEST_EMAIL, TEST_NAME, TEST_PASSWORD, TEST_NICKNAME,
                null);
        MemberSignupResult result = new MemberSignupResult(TEST_EMAIL, TEST_NAME, TEST_NICKNAME, null, TEST_PROVIDER);
        given(memberService.signup(any())).willReturn(result);

        // when & then
        restDocGiven().contentType(ContentType.JSON)
                .body(request)
                .post("/api/v1/members")
                .then()
                .status(HttpStatus.OK)
                .apply(document("api/v1/members/signup", requestPreprocessor(), responsePreprocessor(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                fieldWithPath("profileImageUrl").type(JsonFieldType.NULL).description("프로필 이미지 URL")
                                        .optional()),
                        relaxedResponseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
                                fieldWithPath("data.profileImageUrl").type(JsonFieldType.NULL)
                                        .description("회원 프로필 이미지 URL").optional(),
                                fieldWithPath("data.provider").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyMember_success() {
        // given
        MemberResult result = new MemberResult(TEST_EMAIL, TEST_NAME, TEST_NICKNAME, null, TEST_PROVIDER);
        given(memberService.getMember(any())).willReturn(result);

        restDocGiven()
                .get("/api/v1/members/my")
                .then()
                .status(HttpStatus.OK)
                .apply(document("api/v1/members/my", requestPreprocessor(), responsePreprocessor(),
                        relaxedResponseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                fieldWithPath("data.profileImageUrl").type(JsonFieldType.NULL)
                                        .description("프로필 이미지 URL").optional(),
                                fieldWithPath("data.provider").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 회원 없음")
    void getMyMember_fail_notFound() {
        // given
        given(memberService.getMember(any())).willThrow(new CoreException(ErrorType.MEMBER_NOT_FOUND));

        restDocGiven()
                .get("/api/v1/members/my")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .apply(document("api/v1/members/my-fail", requestPreprocessor(), responsePreprocessor(),
                        relaxedResponseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.NULL).description("추가 에러 데이터")
                                        .optional())));
    }
}
