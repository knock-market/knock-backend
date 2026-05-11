package com.knock.core.domain.seller;

import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.enums.InviteDuration;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.seller.SellerShareLink;
import com.knock.storage.db.core.seller.SellerShareLinkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SellerShareServiceTest {

	@InjectMocks
	private SellerShareService sellerShareService;

	@Mock
	private SellerShareLinkRepository sellerShareLinkRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ItemRepository itemRepository;

	@Nested
	@DisplayName("판매자 공유 링크 생성")
	class CreateShareLink {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(sellerShareLinkRepository.save(any(SellerShareLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			SellerShareLinkCreateResult result = sellerShareService.createShareLink(TEST_MEMBER_ID,
					InviteDuration.ONE_HOUR);

			// then
			assertThat(result.token()).isNotBlank();
			assertThat(result.expiresAt()).isNotNull();
		}

	}

	@Nested
	@DisplayName("공유 링크 판매 페이지 조회")
	class GetSellerShop {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Group group = createGroup(TEST_GROUP_ID, TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, group, member);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN,
					LocalDateTime.now().plusHours(1));

			given(sellerShareLinkRepository.findByToken(TEST_SELLER_SHARE_TOKEN)).willReturn(Optional.of(shareLink));
			given(itemRepository.findByMemberIdWithLikes(TEST_MEMBER_ID))
				.willReturn(List.<Object[]>of(new Object[] { item, TEST_IMAGE_URL, 1L }));

			// when
			SellerShopResult result = sellerShareService.getSellerShop(TEST_SELLER_SHARE_TOKEN);

			// then
			assertThat(result.sellerId()).isEqualTo(TEST_MEMBER_ID);
			assertThat(result.items()).hasSize(1);
		}

		@Test
		@DisplayName("실패 - 만료된 링크")
		void fail_expired() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN,
					LocalDateTime.now().minusMinutes(1));
			given(sellerShareLinkRepository.findByToken(TEST_SELLER_SHARE_TOKEN)).willReturn(Optional.of(shareLink));

			// when & then
			assertThatThrownBy(() -> sellerShareService.getSellerShop(TEST_SELLER_SHARE_TOKEN))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
		}

	}

}
