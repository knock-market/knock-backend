package com.knock.core.api.config;

import com.knock.core.api.controller.v1.ItemController;
import com.knock.core.domain.item.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ItemService itemService;

	@Test
	@DisplayName("비로그인 사용자는 전체 마켓 상품 목록을 조회할 수 있다")
	void guestCanGetMarketplaceItems() throws Exception {
		given(itemService.getMarketplaceItems()).willReturn(List.of());

		mockMvc.perform(get("/api/v1/items")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("비로그인 사용자는 내 판매 상품 목록을 조회할 수 없다")
	void guestCannotGetMySellingItems() throws Exception {
		mockMvc.perform(get("/api/v1/items/my-selling")).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("비로그인 사용자는 상품을 등록할 수 없다")
	void guestCannotCreateItem() throws Exception {
		mockMvc.perform(post("/api/v1/items")).andExpect(status().isForbidden());
	}

}
