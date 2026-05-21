package com.knock.core.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knock.ContextTest;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberJourneyIntegrationTest extends ContextTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("회원가입/로그인/프로필수정/알림설정 수정 플로우")
	void memberProfileAndNotificationSettingsFlow() throws Exception {
		String email = uniqueEmail("member");
		String password = "Password123!";

		signUp(email, "테스트유저", password, "초기닉네임");
		Cookie memberCookie = login(email, password);

		mockMvc.perform(get("/api/v1/members/my").cookie(memberCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(email))
			.andExpect(jsonPath("$.data.nickname").value("초기닉네임"));

		mockMvc
			.perform(put("/api/v1/members/my").cookie(memberCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("nickname", "수정닉네임", "profileImageUrl", "https://example.com/profile.png"))))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/members/my").cookie(memberCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nickname").value("수정닉네임"))
			.andExpect(jsonPath("$.data.profileImageUrl").value("https://example.com/profile.png"));

		mockMvc.perform(get("/api/v1/members/my/settings/notifications").cookie(memberCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.push").value(true))
			.andExpect(jsonPath("$.data.newItems").value(true))
			.andExpect(jsonPath("$.data.chat").value(true))
			.andExpect(jsonPath("$.data.marketing").value(false))
			.andExpect(jsonPath("$.data.sound").value(true));

		mockMvc
			.perform(put("/api/v1/members/my/settings/notifications").cookie(memberCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("push", true, "newItems", false, "chat", false, "marketing", true, "sound", false))))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/members/my/settings/notifications").cookie(memberCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.newItems").value(false))
			.andExpect(jsonPath("$.data.chat").value(false))
			.andExpect(jsonPath("$.data.marketing").value(true))
			.andExpect(jsonPath("$.data.sound").value(false));
	}

	private void signUp(String email, String name, String password, String nickname) throws Exception {
		mockMvc
			.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("email", email, "name", name, "password", password, "nickname", nickname))))
			.andExpect(status().isOk());
	}

	private Cookie login(String email, String password) throws Exception {
		MvcResult result = mockMvc
			.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
			.andExpect(status().isOk())
			.andReturn();
		return result.getResponse().getCookie("SESSION_ID");
	}

	private String uniqueEmail(String prefix) {
		return prefix + "_" + System.nanoTime() + "@test.com";
	}

}
