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

import static com.knock.core.support.TestConstants.TEST_EMAIL;
import static com.knock.core.support.TestConstants.TEST_PASSWORD;
import static com.knock.test.api.RestDocsUtils.requestPreprocessor;
import static com.knock.test.api.RestDocsUtils.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

class AuthControllerTest extends RestDocsTest {

	private AuthService authService;

	private AuthController authController;

	@BeforeEach
	public void setUp() {
		authService = mock(AuthService.class);
		authController = new AuthController(authService);
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

}
