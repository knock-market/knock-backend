package com.knock.core.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knock.ContextTest;
import com.knock.core.api.controller.v1.request.AuthLoginRequestDto;
import com.knock.core.api.controller.v1.request.MemberSignupRequestDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static com.knock.core.support.TestConstants.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserFlowIntegrationTest extends ContextTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("회원가입 -> 내 정보 조회 통합 테스트")
	void signupAndGetMyInfo() throws Exception {
		// 회원가입
		MemberSignupRequestDto signupRequest = new MemberSignupRequestDto(TEST_EMAIL, TEST_NAME, TEST_PASSWORD,
				TEST_NICKNAME, null);
		mockMvc
			.perform(post("/api/v1/members").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
			.andExpect(status().isOk());

		// 로그인
		AuthLoginRequestDto loginRequest = new AuthLoginRequestDto(TEST_EMAIL, TEST_PASSWORD);
		MvcResult loginResult = mockMvc
			.perform(post("/api/v1/auth/login").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isOk())
			.andReturn();

		Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION_ID");

		// 내 정보 조회
		mockMvc.perform(get("/api/v1/members/my").cookie(sessionCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
			.andExpect(jsonPath("$.data.nickname").value(TEST_NICKNAME));
	}

}
