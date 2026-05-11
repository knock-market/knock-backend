package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.SellerShareLinkCreateRequestDto;
import com.knock.core.api.controller.v1.response.SellerShareLinkResponseDto;
import com.knock.core.api.controller.v1.response.SellerShopResponseDto;
import com.knock.core.domain.seller.SellerShareService;
import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.enums.InviteDuration;
import com.knock.core.support.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SellerShareControllerTest {

	@InjectMocks
	private SellerShareController sellerShareController;

	@Mock
	private SellerShareService sellerShareService;

	@Test
	@DisplayName("판매자 공유 링크 생성 성공")
	void createShareLink_success() {
		// given
		MemberPrincipal principal = new MemberPrincipal(TEST_MEMBER_ID, TEST_EMAIL, "ROLE_USER");
		LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
		SellerShareLinkCreateResult result = new SellerShareLinkCreateResult(TEST_SELLER_SHARE_TOKEN, expiresAt);
		given(sellerShareService.createShareLink(TEST_MEMBER_ID, InviteDuration.ONE_HOUR)).willReturn(result);

		// when
		ApiResponse<SellerShareLinkResponseDto> response = sellerShareController.createShareLink(principal,
				new SellerShareLinkCreateRequestDto(InviteDuration.ONE_HOUR));

		// then
		SellerShareLinkResponseDto data = (SellerShareLinkResponseDto) response.getData();
		assertThat(data.token()).isEqualTo(TEST_SELLER_SHARE_TOKEN);
		assertThat(data.path()).isEqualTo("/shop/" + TEST_SELLER_SHARE_TOKEN);
		assertThat(data.expiresAt()).isEqualTo(expiresAt);
	}

	@Test
	@DisplayName("판매자 공유 페이지 조회 성공")
	void getSellerShop_success() {
		// given
		SellerShopResult result = new SellerShopResult(TEST_MEMBER_ID, TEST_NAME, TEST_NICKNAME, TEST_IMAGE_URL,
				List.of());
		given(sellerShareService.getSellerShop(TEST_SELLER_SHARE_TOKEN)).willReturn(result);

		// when
		ApiResponse<SellerShopResponseDto> response = sellerShareController.getSellerShop(TEST_SELLER_SHARE_TOKEN);

		// then
		SellerShopResponseDto data = (SellerShopResponseDto) response.getData();
		assertThat(data.sellerId()).isEqualTo(TEST_MEMBER_ID);
		assertThat(data.sellerName()).isEqualTo(TEST_NAME);
		assertThat(data.sellerNickname()).isEqualTo(TEST_NICKNAME);
		assertThat(data.sellerProfileImageUrl()).isEqualTo(TEST_IMAGE_URL);
		assertThat(data.items()).isEmpty();
	}

}
