package com.knock.core.domain.seller;

import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.enums.InviteDuration;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemListQuery;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.seller.SellerShareLink;
import com.knock.storage.db.core.seller.SellerShareLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SellerShareServiceTest {

	private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Seoul");

	private static final Instant FIXED_INSTANT = Instant.parse("2026-05-29T03:00:00Z");

	private SellerShareService sellerShareService;

	private LocalDateTime fixedNow;

	@Mock
	private SellerShareLinkRepository sellerShareLinkRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ItemRepository itemRepository;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(FIXED_INSTANT, APPLICATION_ZONE);
		fixedNow = LocalDateTime.now(fixedClock);
		sellerShareService = new SellerShareService(sellerShareLinkRepository, memberRepository, itemRepository,
				fixedClock);
	}

	@Nested
	@DisplayName("판매자 공유 링크 생성")
	class CreateShareLink {

		@Test
		@DisplayName("성공 - 1시간 만료")
		void success_oneHour() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			SellerShareLink oldShareLink = SellerShareLink.create(member, "old-share-token", fixedNow.plusHours(1));
			given(memberRepository.findByIdForUpdate(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(sellerShareLinkRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of(oldShareLink));
			given(sellerShareLinkRepository.save(any(SellerShareLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			SellerShareLinkCreateResult result = sellerShareService.createShareLink(TEST_MEMBER_ID,
					InviteDuration.ONE_HOUR);

			// then
			assertThat(result.token()).isNotBlank();
			assertThat(result.expiresAt()).isEqualTo(fixedNow.plusHours(1));
			assertThat(result.active()).isTrue();
			assertThat(result.clickCount()).isZero();
			assertThat(result.useCount()).isZero();
			assertThat(oldShareLink.isActive()).isFalse();
		}

		@Test
		@DisplayName("성공 - 1일 만료")
		void success_oneDay() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findByIdForUpdate(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(sellerShareLinkRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of());
			given(sellerShareLinkRepository.save(any(SellerShareLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			SellerShareLinkCreateResult result = sellerShareService.createShareLink(TEST_MEMBER_ID,
					InviteDuration.ONE_DAY);

			// then
			assertThat(result.expiresAt()).isEqualTo(fixedNow.plusDays(1));
		}

		@Test
		@DisplayName("성공 - 기간이 없으면 1일 만료가 기본값")
		void success_nullDurationDefaultsToOneDay() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findByIdForUpdate(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(sellerShareLinkRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of());
			given(sellerShareLinkRepository.save(any(SellerShareLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			SellerShareLinkCreateResult result = sellerShareService.createShareLink(TEST_MEMBER_ID, null);

			// then
			assertThat(result.expiresAt()).isEqualTo(fixedNow.plusDays(1));
		}

		@Test
		@DisplayName("성공 - 영구 링크는 만료 시간이 없다")
		void success_permanent() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findByIdForUpdate(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(sellerShareLinkRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of());
			given(sellerShareLinkRepository.save(any(SellerShareLink.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			SellerShareLinkCreateResult result = sellerShareService.createShareLink(TEST_MEMBER_ID,
					InviteDuration.PERMANENT);

			// then
			assertThat(result.expiresAt()).isNull();
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
			Item item = createItem(TEST_ITEM_ID, member);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN, fixedNow.plusHours(1));

			given(sellerShareLinkRepository.findByTokenForUpdate(TEST_SELLER_SHARE_TOKEN))
				.willReturn(Optional.of(shareLink));
			given(itemRepository.findPublicListingsByMemberIdWithLikes(eq(TEST_MEMBER_ID), any(ItemListQuery.class)))
				.willReturn(List.<Object[]>of(new Object[] { item, TEST_IMAGE_URL, 1L }));

			// when
			SellerShopResult result = sellerShareService.getSellerShop(TEST_SELLER_SHARE_TOKEN);

			// then
			assertThat(result.sellerId()).isEqualTo(TEST_MEMBER_ID);
			assertThat(result.items()).hasSize(1);
			assertThat(shareLink.getClickCount()).isEqualTo(1);
			assertThat(shareLink.getUseCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("성공 - 만료 시간이 현재 시각과 같으면 사용 가능")
		void success_exactExpiryBoundary() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN, fixedNow);

			given(sellerShareLinkRepository.findByTokenForUpdate(TEST_SELLER_SHARE_TOKEN))
				.willReturn(Optional.of(shareLink));
			given(itemRepository.findPublicListingsByMemberIdWithLikes(eq(TEST_MEMBER_ID), any(ItemListQuery.class)))
				.willReturn(List.<Object[]>of(new Object[] { item, TEST_IMAGE_URL, 1L }));

			// when
			SellerShopResult result = sellerShareService.getSellerShop(TEST_SELLER_SHARE_TOKEN);

			// then
			assertThat(result.sellerId()).isEqualTo(TEST_MEMBER_ID);
			assertThat(shareLink.getClickCount()).isEqualTo(1);
			assertThat(shareLink.getUseCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("실패 - 만료 시간이 현재 시각보다 이전이면 클릭만 기록")
		void fail_expired() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN, fixedNow.minusNanos(1));
			given(sellerShareLinkRepository.findByTokenForUpdate(TEST_SELLER_SHARE_TOKEN))
				.willReturn(Optional.of(shareLink));

			// when & then
			assertThatThrownBy(() -> sellerShareService.getSellerShop(TEST_SELLER_SHARE_TOKEN))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
			assertThat(shareLink.getClickCount()).isEqualTo(1);
			assertThat(shareLink.getUseCount()).isZero();
		}

	}

	@Nested
	@DisplayName("내 공유 링크 관리")
	class ManageShareLinks {

		@Test
		@DisplayName("목록 조회 성공")
		void getMyShareLinks_success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN, fixedNow.plusHours(1));
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(sellerShareLinkRepository.findLatestByMemberId(TEST_MEMBER_ID)).willReturn(Optional.of(shareLink));

			// when & then
			assertThat(sellerShareService.getMyShareLinks(TEST_MEMBER_ID)).hasSize(1);
			verify(sellerShareLinkRepository).findLatestByMemberId(TEST_MEMBER_ID);
		}

		@Test
		@DisplayName("공유 중단 성공")
		void deactivateShareLink_success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			SellerShareLink shareLink = SellerShareLink.create(member, TEST_SELLER_SHARE_TOKEN, fixedNow.plusHours(1));
			given(sellerShareLinkRepository.findByTokenForUpdate(TEST_SELLER_SHARE_TOKEN))
				.willReturn(Optional.of(shareLink));

			// when
			sellerShareService.deactivateShareLink(TEST_MEMBER_ID, TEST_SELLER_SHARE_TOKEN);

			// then
			assertThat(shareLink.isActive()).isFalse();
		}

	}

}
