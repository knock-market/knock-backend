package com.knock.core.api.config;

import com.knock.core.api.controller.v1.ItemController;
import com.knock.core.api.controller.v1.SellerShareController;
import com.knock.core.domain.item.ItemService;
import com.knock.core.domain.seller.SellerShareService;
import com.knock.core.enums.ItemType;
import com.knock.core.enums.ItemStatus;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ ItemController.class, SellerShareController.class })
@Import(SecurityConfig.class)
class SecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ItemService itemService;

	@MockBean
	private SellerShareService sellerShareService;

	@Test
	@DisplayName("비로그인 사용자는 전체 마켓 상품 목록을 조회할 수 없다")
	void guestCannotGetMarketplaceItems() throws Exception {
		mockMvc.perform(get("/api/v1/items"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A003"));
	}

	@Test
	@DisplayName("비로그인 사용자는 내 판매 상품 목록을 조회할 수 없다")
	void guestCannotGetMySellingItems() throws Exception {
		mockMvc.perform(get("/api/v1/items/my-selling"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A003"));
	}

	@Test
	@DisplayName("비로그인 사용자는 상품을 등록할 수 없다")
	void guestCannotCreateItem() throws Exception {
		mockMvc.perform(post("/api/v1/items"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A003"));
	}

	@Test
	@DisplayName("비로그인 사용자는 공개 판매자 매대 matcher를 통과한다")
	void guestCanReachPublicSellerShareShelfMatcher() throws Exception {
		given(sellerShareService.getSellerShop(eq("public-token"), any()))
			.willReturn(new SellerShopResult(1L, "seller", "seller", null, List.of()));

		mockMvc.perform(get("/api/v1/seller-shares/public-token")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("비로그인 사용자는 공개 공유 상품 matcher를 통과한다")
	void guestCanReachPublicSellerShareItemMatcher() throws Exception {
		given(sellerShareService.getSharedItem("public-token", "123e4567-e89b-12d3-a456-426614174000"))
			.willReturn(new ItemReadResult(1L, "123e4567-e89b-12d3-a456-426614174000", "item", "desc", 1000L,
					ItemType.SELL, ItemStatus.ON_SALE, List.of(), 2L, "seller", null));

		mockMvc.perform(get("/api/v1/seller-shares/public-token/items/123e4567-e89b-12d3-a456-426614174000"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("비로그인 사용자는 내 공유 링크 목록을 조회할 수 없다")
	void guestCannotGetMySellerShares() throws Exception {
		mockMvc.perform(get("/api/v1/seller-shares/my"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A003"));
	}

	@Test
	@DisplayName("비로그인 사용자는 공유 링크 멤버십을 생성할 수 없다")
	void guestCannotCreateSellerShareMembership() throws Exception {
		mockMvc.perform(post("/api/v1/seller-shares/public-token/memberships"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A003"));
	}

}
