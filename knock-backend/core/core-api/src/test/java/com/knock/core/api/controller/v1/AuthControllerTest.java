package com.knock.core.api.controller.v1;

import com.knock.auth.AuthService;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.AuthLoginRequestDto;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;

import static com.knock.core.support.TestConstants.TEST_EMAIL;
import static com.knock.core.support.TestConstants.TEST_PASSWORD;
import static com.knock.test.api.RestDocsUtils.requestPreprocessor;
import static com.knock.test.api.RestDocsUtils.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

class AuthControllerTest extends RestDocsTest {

	private AuthService authService;

	private AuthController authController;

	@BeforeEach
	public void setUp() {
		authService = mock(AuthService.class);
		authController = new AuthController(authService);
		ReflectionTestUtils.setField(authController, "googleLoginSuccessRedirectUri", "http://localhost:3000/#/home");
		mockMvc = mockController(authController, new ApiControllerAdvice());
	}

	@Test
	@DisplayName("로그인 성공")
	void login_success() {
		// given
		AuthLoginRequestDto request = new AuthLoginRequestDto(TEST_EMAIL, TEST_PASSWORD);
		doNothing().when(authService).login(any(), any(), any());

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/auth/login")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/auth/login", requestPreprocessor(), responsePreprocessor(),
					requestFields(fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
							fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")),
					responseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("로그인 실패 - 이메일 누락")
	void login_fail_missingEmail() {
		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body("{\"password\":\"" + TEST_PASSWORD + "\"}")
			.post("/api/v1/auth/login")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("로그인 실패 - 이메일 공백")
	void login_fail_blankEmail() {
		// given
		AuthLoginRequestDto request = new AuthLoginRequestDto(" ", TEST_PASSWORD);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/auth/login")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("로그인 실패 - 이메일 형식 오류")
	void login_fail_invalidEmail() {
		// given
		AuthLoginRequestDto request = new AuthLoginRequestDto("invalid-email", TEST_PASSWORD);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/auth/login")
			.then()
			.status(HttpStatus.BAD_REQUEST)
			.apply(document("api/v1/auth/login-fail-validation", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
							fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
							fieldWithPath("error.data.email").type(JsonFieldType.STRING).description("이메일 검증 오류"))));

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 누락")
	void login_fail_missingPassword() {
		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body("{\"email\":\"" + TEST_EMAIL + "\"}")
			.post("/api/v1/auth/login")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 공백")
	void login_fail_blankPassword() {
		// given
		AuthLoginRequestDto request = new AuthLoginRequestDto(TEST_EMAIL, " ");

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/auth/login")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(authService);
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logout_success() {
		// given
		doNothing().when(authService).logout(any());

		// when & then
		restDocGiven().post("/api/v1/auth/logout")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/auth/logout", requestPreprocessor(), responsePreprocessor(),
					responseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("구글 OAuth 시작 리다이렉트 성공")
	void start_google_login_success() {
		// given
		String authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=test";
		doReturn(authorizationUrl).when(authService).getGoogleAuthorizationUrl(anyString(), any());

		// when & then
		restDocGiven().get("/api/v1/auth/social/google/start").then().status(HttpStatus.FOUND);
	}

	@Test
	@DisplayName("구글 OAuth 콜백 성공")
	void google_callback_success() {
		// given
		doReturn("/item/1").when(authService).loginWithGoogle(any(), any(), any(), any());

		// when & then
		restDocGiven().queryParam("code", "auth-code")
			.queryParam("state", "state-token")
			.get("/api/v1/auth/social/google/callback")
			.then()
			.status(HttpStatus.FOUND)
			.header("Location", "http://localhost:3000/#/item/1");
	}

}
