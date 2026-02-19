package com.knock.core.domain.user;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberGroupJourneyIntegrationTest extends ContextTest {

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

	@Test
	@DisplayName("그룹 생성/상세조회/초대코드 가입/중복가입 실패/잘못된 코드 실패 플로우")
	void groupInviteJoinFlowWithFailureCases() throws Exception {
		String ownerEmail = uniqueEmail("owner");
		String ownerPassword = "Password123!";
		signUp(ownerEmail, "그룹장", ownerPassword, "그룹장닉");
		Cookie ownerCookie = login(ownerEmail, ownerPassword);

		MvcResult createGroupResult = mockMvc
			.perform(post("/api/v1/groups").cookie(ownerCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("name", "E2E 그룹", "description", "통합테스트 그룹", "imageUrl",
						"https://example.com/group.png"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").exists())
			.andExpect(jsonPath("$.data.inviteCode").exists())
			.andReturn();

		JsonNode groupData = readData(createGroupResult);
		long groupId = groupData.path("id").asLong();
		String inviteCode = groupData.path("inviteCode").asText();
		assertThat(inviteCode).isNotBlank();

		mockMvc.perform(get("/api/v1/groups/{groupId}", groupId).cookie(ownerCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(groupId))
			.andExpect(jsonPath("$.data.inviteCode").doesNotExist());

		String memberEmail = uniqueEmail("member");
		String memberPassword = "Password123!";
		signUp(memberEmail, "그룹원", memberPassword, "그룹원닉");
		Cookie memberCookie = login(memberEmail, memberPassword);

		mockMvc
			.perform(post("/api/v1/groups/join").cookie(memberCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("inviteCode", inviteCode))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(groupId));

		mockMvc
			.perform(post("/api/v1/groups/join").cookie(memberCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("inviteCode", inviteCode))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("G002"));

		String outsiderEmail = uniqueEmail("outsider");
		String outsiderPassword = "Password123!";
		signUp(outsiderEmail, "외부유저", outsiderPassword, "외부닉");
		Cookie outsiderCookie = login(outsiderEmail, outsiderPassword);

		mockMvc
			.perform(post("/api/v1/groups/join").cookie(outsiderCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("inviteCode", "INVALID"))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("G001"));
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

	private JsonNode readData(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
	}

	private String uniqueEmail(String prefix) {
		return prefix + "_" + System.nanoTime() + "@test.com";
	}

}
