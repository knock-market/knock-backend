package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ItemListRequestDto;
import com.knock.core.api.controller.v1.response.ItemResponseDto;
import com.knock.core.api.controller.v1.response.SellerAccessMembershipResponseDto;
import com.knock.core.api.controller.v1.request.SellerShareLinkCreateRequestDto;
import com.knock.core.api.controller.v1.response.SellerShareLinkResponseDto;
import com.knock.core.api.controller.v1.response.SellerShareLinkSummaryResponseDto;
import com.knock.core.api.controller.v1.response.SellerShopResponseDto;
import com.knock.core.domain.seller.SellerShareService;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.domain.seller.dto.SellerAccessMembershipResult;
import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShareLinkStatsResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.enums.InviteDuration;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
		SellerShareLinkCreateResult result = new SellerShareLinkCreateResult(TEST_SELLER_SHARE_TOKEN, expiresAt, true,
				0L, 0L);
		given(sellerShareService.createShareLink(TEST_MEMBER_ID, InviteDuration.ONE_HOUR)).willReturn(result);

		// when
		ApiResponse<SellerShareLinkResponseDto> response = sellerShareController.createShareLink(principal,
				new SellerShareLinkCreateRequestDto(InviteDuration.ONE_HOUR));

		// then
		SellerShareLinkResponseDto data = (SellerShareLinkResponseDto) response.getData();
		assertThat(data.token()).isEqualTo(TEST_SELLER_SHARE_TOKEN);
		assertThat(data.path()).isEqualTo("/shop/" + TEST_SELLER_SHARE_TOKEN);
		assertThat(data.expiresAt()).isEqualTo(expiresAt);
		assertThat(data.active()).isTrue();
	}

	@Test
	@DisplayName("판매자 공유 페이지 조회 성공")
	void getSellerShop_success() {
		// given
		SellerShopResult result = new SellerShopResult(TEST_MEMBER_ID, TEST_NAME, TEST_NICKNAME, TEST_IMAGE_URL,
				List.of());
		given(sellerShareService.getSellerShop(eq(TEST_SELLER_SHARE_TOKEN), any())).willReturn(result);

		// when
		ApiResponse<SellerShopResponseDto> response = sellerShareController.getSellerShop(TEST_SELLER_SHARE_TOKEN,
				new ItemListRequestDto(null, null, null, null, null, null));

		// then
		SellerShopResponseDto data = (SellerShopResponseDto) response.getData();
		assertThat(data.sellerId()).isEqualTo(TEST_MEMBER_ID);
		assertThat(data.sellerName()).isEqualTo(TEST_NAME);
		assertThat(data.sellerNickname()).isEqualTo(TEST_NICKNAME);
		assertThat(data.sellerProfileImageUrl()).isEqualTo(TEST_IMAGE_URL);
		assertThat(data.items()).isEmpty();
	}

	@Test
	@DisplayName("공유 링크 상품 상세 조회 성공")
	void getSharedItem_success() {
		// given
		ItemReadResult result = new ItemReadResult(TEST_ITEM_ID, TEST_ITEM_PUBLIC_ID, TEST_ITEM_TITLE,
				TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE, ItemType.SELL, ItemStatus.ON_SALE, List.of(TEST_IMAGE_URL),
				TEST_MEMBER_ID, TEST_NICKNAME, TEST_IMAGE_URL, TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
				TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
		given(sellerShareService.getSharedItem(TEST_SELLER_SHARE_TOKEN, TEST_ITEM_PUBLIC_ID)).willReturn(result);

		// when
		ApiResponse<ItemResponseDto> response = sellerShareController.getSharedItem(TEST_SELLER_SHARE_TOKEN,
				TEST_ITEM_PUBLIC_ID);

		// then
		ItemResponseDto data = (ItemResponseDto) response.getData();
		assertThat(data.publicId()).isEqualTo(TEST_ITEM_PUBLIC_ID);
		assertThat(data.writerId()).isEqualTo(TEST_MEMBER_ID);
	}

	@Test
	@DisplayName("공유 링크 멤버십 생성 성공")
	void createMembership_success() {
		// given
		MemberPrincipal principal = new MemberPrincipal(TEST_MEMBER_ID_2, TEST_EMAIL_2, "ROLE_USER");
		LocalDateTime createdAt = LocalDateTime.now();
		SellerAccessMembershipResult result = new SellerAccessMembershipResult(TEST_MEMBER_ID, TEST_MEMBER_ID_2,
				"ACTIVE", createdAt);
		given(sellerShareService.createMembership(TEST_MEMBER_ID_2, TEST_SELLER_SHARE_TOKEN)).willReturn(result);

		// when
		ApiResponse<SellerAccessMembershipResponseDto> response = sellerShareController.createMembership(principal,
				TEST_SELLER_SHARE_TOKEN);

		// then
		SellerAccessMembershipResponseDto data = (SellerAccessMembershipResponseDto) response.getData();
		assertThat(data.sellerId()).isEqualTo(TEST_MEMBER_ID);
		assertThat(data.memberId()).isEqualTo(TEST_MEMBER_ID_2);
		assertThat(data.status()).isEqualTo("ACTIVE");
		assertThat(data.createdAt()).isEqualTo(createdAt);
	}

	@Test
	@DisplayName("내 공유 링크 목록 조회 성공")
	void getMyShareLinks_success() {
		// given
		MemberPrincipal principal = new MemberPrincipal(TEST_MEMBER_ID, TEST_EMAIL, "ROLE_USER");
		LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
		SellerShareLinkStatsResult result = new SellerShareLinkStatsResult(TEST_SELLER_SHARE_TOKEN, expiresAt, true, 3L,
				2L, LocalDateTime.now());
		given(sellerShareService.getMyShareLinks(TEST_MEMBER_ID)).willReturn(List.of(result));

		// when
		ApiResponse<List<SellerShareLinkSummaryResponseDto>> response = sellerShareController
			.getMyShareLinks(principal);

		// then
		List<SellerShareLinkSummaryResponseDto> data = (List<SellerShareLinkSummaryResponseDto>) response.getData();
		assertThat(data).hasSize(1);
		assertThat(data.get(0).clickCount()).isEqualTo(3L);
		assertThat(data.get(0).useCount()).isEqualTo(2L);
	}

}
