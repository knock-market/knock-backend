package com.knock.core.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knock.core.domain.group.GroupService;
import com.knock.core.domain.group.dto.GroupCreateData;
import com.knock.core.domain.group.dto.GroupJoinData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private GroupService groupService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("그룹 생성 API")
	void createGroup() throws Exception {
		// given
		Long memberId = 1L;
		GroupCreateData data = new GroupCreateData("New Group", "Desc");
		given(groupService.createGroup(eq(memberId), any())).willReturn(10L);

		// when & then
		mockMvc
			.perform(post("/api/v1/groups").header("X-User-Id", memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(data)))
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("그룹 가입 API")
	void joinGroup() throws Exception {
		// given
		Long memberId = 2L;
		GroupJoinData request = new GroupJoinData("CODE1234");
		given(groupService.joinGroup(eq(memberId), any())).willReturn(10L);

		// when & then
		mockMvc
			.perform(post("/api/v1/groups/join").header("X-User-Id", memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isOk());
	}

}
