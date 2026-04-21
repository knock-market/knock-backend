package com.knock.core.api.controller;

import com.knock.ContextTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ApiControllerAdviceIntegrationTest extends ContextTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("정적 리소스 요청 미존재시 404를 반환한다")
	void notFoundResource_returns404() throws Exception {
		mockMvc.perform(get("/favicon.ico"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.error.code").value("E404"));
	}

}
