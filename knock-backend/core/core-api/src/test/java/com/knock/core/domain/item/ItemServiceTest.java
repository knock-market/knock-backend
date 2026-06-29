package com.knock.core.domain.item;

import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ItemType;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemListQuery;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@InjectMocks
	private ItemService itemService;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private SellerAccessMemberRepository sellerAccessMemberRepository;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Nested
	@DisplayName("상품 등록")
	class CreateItem {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);
			ItemCreateData data = new ItemCreateData(TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
					ItemType.SELL, List.of(TEST_IMAGE_URL), TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
					TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);

			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(itemRepository.save(any(Item.class), anyList())).willReturn(item);

			// when
			ItemCreateResult result = itemService.createItem(TEST_MEMBER_ID, data);

			// then
			assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
			assertThat(result.publicId()).isEqualTo(TEST_ITEM_PUBLIC_ID);
		}

		@Test
		@DisplayName("실패 - 거래 위치 좌표 범위 초과")
		void fail_invalidTradeLocation() {
			// given
			ItemCreateData data = new ItemCreateData(TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
					ItemType.SELL, List.of(), TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS, 91.0,
					TEST_TRADE_LONGITUDE);

			// when & then
			assertThatThrownBy(() -> itemService.createItem(TEST_MEMBER_ID, data)).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.VALIDATION_ERROR);
		}

		@Test
		@DisplayName("실패 - 거래 위치 미지정")
		void fail_missingTradeLocation() {
			// given
			ItemCreateData data = new ItemCreateData(TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
					ItemType.SELL, List.of(TEST_IMAGE_URL), null, null, null, null);

			// when & then
			assertThatThrownBy(() -> itemService.createItem(TEST_MEMBER_ID, data)).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.VALIDATION_ERROR);
		}

		@Test
		@DisplayName("실패 - 회원 없음")
		void fail_memberNotFound() {
			// given
			ItemCreateData data = new ItemCreateData(TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
					ItemType.SELL, List.of(), TEST_TRADE_LOCATION_NAME, TEST_TRADE_LOCATION_ADDRESS,
					TEST_TRADE_LATITUDE, TEST_TRADE_LONGITUDE);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> itemService.createItem(TEST_MEMBER_ID, data)).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
		}

	}

	@Nested
	@DisplayName("상품 조회")
	class GetItem {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);

			given(itemRepository.findByIdWithImages(TEST_ITEM_ID)).willReturn(Optional.of(item));

			// when
			ItemReadResult result = itemService.getItem(TEST_ITEM_ID);

			// then
			assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
			assertThat(result.publicId()).isEqualTo(TEST_ITEM_PUBLIC_ID);
			assertThat(result.title()).isEqualTo(TEST_ITEM_TITLE);
		}

		@Test
		@DisplayName("성공 - 관리용 조회: 판매자 본인")
		void success_getItemForOwner() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);
			given(itemRepository.findByIdWithImages(TEST_ITEM_ID)).willReturn(Optional.of(item));

			// when
			ItemReadResult result = itemService.getItemForOwner(TEST_MEMBER_ID, TEST_ITEM_ID);

			// then
			assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
		}

		@Test
		@DisplayName("실패 - 관리용 조회: 판매자 아님")
		void fail_getItemForOwnerForbidden() {
			// given
			Member seller = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, seller);
			given(itemRepository.findByIdWithImages(TEST_ITEM_ID)).willReturn(Optional.of(item));

			// when & then
			assertThatThrownBy(() -> itemService.getItemForOwner(TEST_MEMBER_ID_2, TEST_ITEM_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		}

		@Test
		@DisplayName("성공 - 공개 식별자로 조회: 판매자 본인")
		void success_publicId_owner() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);

			given(itemRepository.findByPublicIdWithImages(TEST_ITEM_PUBLIC_ID)).willReturn(Optional.of(item));

			// when
			ItemReadResult result = itemService.getItemByPublicId(TEST_MEMBER_ID, TEST_ITEM_PUBLIC_ID);

			// then
			assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
			assertThat(result.publicId()).isEqualTo(TEST_ITEM_PUBLIC_ID);
		}

		@Test
		@DisplayName("성공 - 공개 식별자로 조회: 초대된 접근 회원")
		void success_publicId_accessMember() {
			// given
			Member seller = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, seller);
			given(itemRepository.findByPublicIdWithImages(TEST_ITEM_PUBLIC_ID)).willReturn(Optional.of(item));
			given(sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
				.willReturn(true);

			// when
			ItemReadResult result = itemService.getItemByPublicId(TEST_MEMBER_ID_2, TEST_ITEM_PUBLIC_ID);

			// then
			assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
		}

		@Test
		@DisplayName("실패 - 공개 식별자 조회: 초대되지 않은 회원")
		void fail_publicIdForbidden() {
			// given
			Member seller = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, seller);
			given(itemRepository.findByPublicIdWithImages(TEST_ITEM_PUBLIC_ID)).willReturn(Optional.of(item));

			// when & then
			assertThatThrownBy(() -> itemService.getItemByPublicId(TEST_MEMBER_ID_2, TEST_ITEM_PUBLIC_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		}

		@Test
		@DisplayName("실패 - 상품 없음")
		void fail_itemNotFound() {
			// given
			given(itemRepository.findByIdWithImages(TEST_ITEM_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> itemService.getItem(TEST_ITEM_ID)).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.ITEM_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 공개 식별자 상품 없음")
		void fail_publicIdItemNotFound() {
			// given
			given(itemRepository.findByPublicIdWithImages(TEST_ITEM_PUBLIC_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> itemService.getItemByPublicId(TEST_MEMBER_ID, TEST_ITEM_PUBLIC_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.ITEM_NOT_FOUND);
		}

	}

	@Nested
	@DisplayName("내 판매 상품 조회")
	class GetMySellingItems {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);

			List<Object[]> mockResult = new ArrayList<>();
			mockResult.add(new Object[] { item, TEST_IMAGE_URL, 3L });
			given(itemRepository.findOwnerInventoryWithLikes(TEST_MEMBER_ID)).willReturn(mockResult);

			// when
			List<ItemListResult> results = itemService.getMySellingItems(TEST_MEMBER_ID);

			// then
			assertThat(results).hasSize(1);
			assertThat(results.getFirst().id()).isEqualTo(TEST_ITEM_ID);
		}

	}

	@Nested
	@DisplayName("회원 판매 상품 조회")
	class GetSellingItemsByMember {

		@Test
		@DisplayName("성공 - 판매자 본인")
		void success_owner() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, member);

			List<Object[]> mockResult = new ArrayList<>();
			mockResult.add(new Object[] { item, TEST_IMAGE_URL, 3L });
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(itemRepository.findPublicListingsByMemberIdWithLikes(eq(TEST_MEMBER_ID), any(ItemListQuery.class)))
				.willReturn(mockResult);

			// when
			List<ItemListResult> results = itemService.getSellingItemsByMember(TEST_MEMBER_ID, TEST_MEMBER_ID,
					ItemListQuery.defaultQuery());

			// then
			assertThat(results).hasSize(1);
			assertThat(results.getFirst().writerId()).isEqualTo(TEST_MEMBER_ID);
		}

		@Test
		@DisplayName("성공 - 초대된 접근 회원")
		void success_accessMember() {
			// given
			Member seller = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, seller);
			List<Object[]> mockResult = new ArrayList<>();
			mockResult.add(new Object[] { item, TEST_IMAGE_URL, 3L });
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(seller));
			given(sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
				.willReturn(true);
			given(itemRepository.findPublicListingsByMemberIdWithLikes(eq(TEST_MEMBER_ID), any(ItemListQuery.class)))
				.willReturn(mockResult);

			// when
			List<ItemListResult> results = itemService.getSellingItemsByMember(TEST_MEMBER_ID_2, TEST_MEMBER_ID,
					ItemListQuery.defaultQuery());

			// then
			assertThat(results).hasSize(1);
		}

		@Test
		@DisplayName("실패 - 초대되지 않은 회원")
		void fail_forbidden() {
			// given
			Member seller = createMember(TEST_MEMBER_ID);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(seller));

			// when & then
			assertThatThrownBy(() -> itemService.getSellingItemsByMember(TEST_MEMBER_ID_2, TEST_MEMBER_ID,
					ItemListQuery.defaultQuery())).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		}

		@Test
		@DisplayName("실패 - 회원 없음")
		void fail_memberNotFound() {
			// given
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> itemService.getSellingItemsByMember(TEST_MEMBER_ID, TEST_MEMBER_ID,
					ItemListQuery.defaultQuery())).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
		}

	}

}
