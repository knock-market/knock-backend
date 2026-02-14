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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TradeSocialJourneyIntegrationTest extends ContextTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("거래 플로우: 그룹/상품/예약/승인/완료 + 권한 실패 + 북마크 토글")
	void tradeFlowWithPermissionChecks() throws Exception {
		String sellerEmail = uniqueEmail("seller");
		String buyerEmail = uniqueEmail("buyer");
		String strangerEmail = uniqueEmail("stranger");
		String password = "Password123!";

		signUp(sellerEmail, "판매자", password, "판매자닉");
		signUp(buyerEmail, "구매자", password, "구매자닉");
		signUp(strangerEmail, "제3자", password, "제3자닉");

		Cookie sellerCookie = login(sellerEmail, password);
		Cookie buyerCookie = login(buyerEmail, password);
		Cookie strangerCookie = login(strangerEmail, password);

		MvcResult createGroupResult = mockMvc
			.perform(post("/api/v1/groups").cookie(sellerCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						Map.of("name", "거래 그룹", "description", "거래용", "imageUrl", "https://example.com/group.png"))))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode createdGroup = readData(createGroupResult);
		long groupId = createdGroup.path("id").asLong();
		String inviteCode = createdGroup.path("inviteCode").asText();

		MvcResult createItemResult = mockMvc.perform(post("/api/v1/items").cookie(sellerCookie)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of("groupId", groupId, "title", "맥북", "description", "거의 새 제품",
					"price", 1500000, "itemType", "SELL", "category", "DIGITAL_DEVICE", "imageUrls", List.of()))))
			.andExpect(status().isOk())
			.andReturn();

		long itemId = readData(createItemResult).path("id").asLong();

		mockMvc
			.perform(post("/api/v1/groups/join").cookie(buyerCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("inviteCode", inviteCode))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(groupId));

		MvcResult reserveResult = mockMvc
			.perform(post("/api/v1/reservations").cookie(buyerCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("itemId", itemId))))
			.andExpect(status().isOk())
			.andReturn();

		long reservationId = readData(reserveResult).path("reservationId").asLong();

		// 구매자는 승인 권한이 없어야 함
		mockMvc.perform(patch("/api/v1/reservations/{id}/approve", reservationId).cookie(buyerCookie))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("E403"));

		mockMvc.perform(patch("/api/v1/reservations/{id}/approve", reservationId).cookie(sellerCookie))
			.andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/reservations/{id}/complete", reservationId).cookie(buyerCookie))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/reservations/my").cookie(buyerCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].id").value(reservationId))
			.andExpect(jsonPath("$.data[0].status").value("COMPLETED"));

		mockMvc.perform(post("/api/v1/items/{itemId}/bookmarks", itemId).cookie(buyerCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.toggleOn").value(true));

		mockMvc.perform(post("/api/v1/items/{itemId}/bookmarks", itemId).cookie(buyerCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.toggleOn").value(false));

		// 제3자는 삭제 권한이 없어야 함
		mockMvc.perform(delete("/api/v1/items/{itemId}", itemId).cookie(strangerCookie))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("E403"));
	}

	@Test
	@DisplayName("소셜 플로우: 유저 차단/목록/해제 + 자기 자신 차단 실패")
	void blockAndUnblockFlowWithValidationFailure() throws Exception {
		String blockerEmail = uniqueEmail("blocker");
		String targetEmail = uniqueEmail("target");
		String password = "Password123!";

		signUp(blockerEmail, "차단자", password, "차단자닉");
		signUp(targetEmail, "대상자", password, "대상자닉");

		Cookie blockerCookie = login(blockerEmail, password);
		Cookie targetCookie = login(targetEmail, password);

		long blockerId = getMyId(blockerCookie);
		long targetId = getMyId(targetCookie);
		assertThat(blockerId).isNotEqualTo(targetId);

		mockMvc.perform(post("/api/v1/members/{memberId}/block", targetId).cookie(blockerCookie))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/members/my/blocked").cookie(blockerCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].id").value(targetId));

		mockMvc.perform(delete("/api/v1/members/{memberId}/block", targetId).cookie(blockerCookie))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/members/my/blocked").cookie(blockerCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data").isEmpty());

		mockMvc.perform(post("/api/v1/members/{memberId}/block", blockerId).cookie(blockerCookie))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("E400"));
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

	private long getMyId(Cookie cookie) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/v1/members/my").cookie(cookie))
			.andExpect(status().isOk())
			.andReturn();
		return readData(result).path("id").asLong();
	}

	private JsonNode readData(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
	}

	private String uniqueEmail(String prefix) {
		return prefix + "_" + System.nanoTime() + "@test.com";
	}

}
