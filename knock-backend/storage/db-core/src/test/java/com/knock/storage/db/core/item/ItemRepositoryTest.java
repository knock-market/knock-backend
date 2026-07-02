package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemListSort;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.bookmark.BookmarkRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.seller.SellerAccessMember;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
import com.knock.storage.db.core.seller.SellerShareLink;
import com.knock.storage.db.core.seller.SellerShareLinkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTest extends CoreDbContextTest {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private SellerShareLinkRepository sellerShareLinkRepository;

	@Autowired
	private SellerAccessMemberRepository sellerAccessMemberRepository;

	@Test
	@DisplayName("상품 저장 및 조회 성공")
	void saveAndFindItem() {
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = Item.create(member, "Title", "Desc", 1000L, ItemType.SELL);

		Item savedItem = itemRepository.save(item, List.of("http://image.url"));
		Optional<Item> foundItem = itemRepository.findById(savedItem.getId());

		assertThat(foundItem).isPresent();
		assertThat(foundItem.get().getTitle()).isEqualTo("Title");
		assertThat(foundItem.get().getPublicId()).isNotBlank();
	}

	@Test
	@DisplayName("상품 공개 식별자로 조회 성공")
	void findByPublicId() {
		Member member = memberRepository.save(Member.create("public@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = Item.create(member, "Public Item", "Desc", 1000L, ItemType.SELL);

		Item savedItem = itemRepository.save(item, List.of());
		Optional<Item> foundItem = itemRepository.findByPublicId(savedItem.getPublicId());

		assertThat(foundItem).isPresent();
		assertThat(foundItem.get().getId()).isEqualTo(savedItem.getId());
	}

	@Test
	@DisplayName("개인 상품 저장 및 조회 성공")
	void saveAndFindPersonalItem() {
		Member member = memberRepository.save(Member.create("personal@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = Item.create(member, "Personal Item", "Desc", 1000L, ItemType.SELL);

		Item savedItem = itemRepository.save(item, List.of());
		Optional<Item> foundItem = itemRepository.findById(savedItem.getId());

		assertThat(foundItem).isPresent();
		assertThat(foundItem.get().getMember().getId()).isEqualTo(member.getId());
		assertThat(foundItem.get().getTitle()).isEqualTo("Personal Item");
	}

	@Test
	@DisplayName("상품 거래 위치 저장 및 조회 성공")
	void saveAndFindTradeLocation() {
		Member member = memberRepository.save(Member.create("location@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = Item.create(member, "Location Item", "Desc", 1000L, ItemType.SELL);
		item.updateTradeLocation("학생회관", "서울특별시 성북구 안암로 145", 37.589387, 127.032477);

		Item savedItem = itemRepository.save(item, List.of());
		Optional<Item> foundItem = itemRepository.findById(savedItem.getId());

		assertThat(foundItem).isPresent();
		assertThat(foundItem.get().getTradeLocationName()).isEqualTo("학생회관");
		assertThat(foundItem.get().getTradeLocationAddress()).isEqualTo("서울특별시 성북구 안암로 145");
		assertThat(foundItem.get().getTradeLatitude()).isEqualTo(37.589387);
		assertThat(foundItem.get().getTradeLongitude()).isEqualTo(127.032477);
	}

	@Test
	@DisplayName("공개 목록 query는 키워드/위치/status/page를 함께 적용한다")
	void findPublicListingsWithLikes_appliesQueryContract() {
		Member seller = memberRepository.save(Member.create("seller@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item target = createLocatedItem(seller, "MacBook Pro", "Clean laptop", 1200L, "Gangnam Station", "Seoul");
		Item otherLocation = createLocatedItem(seller, "MacBook Air", "Clean laptop", 900L, "Hongdae", "Mapo");
		Item sold = createLocatedItem(seller, "MacBook Sold", "Clean laptop", 800L, "Gangnam Station", "Seoul");
		ReflectionTestUtils.setField(sold, "status", ItemStatus.SOLD);
		itemRepository.save(target, List.of());
		itemRepository.save(otherLocation, List.of());
		itemRepository.save(sold, List.of());

		ItemListQuery query = new ItemListQuery("macbook", "gangnam", ItemStatus.ON_SALE, ItemListSort.LATEST, 0, 20);

		List<Object[]> rows = itemRepository.findPublicListingsWithLikes(query);

		assertThat(extractItems(rows)).containsExactly(target);
	}

	@Test
	@DisplayName("공개 목록 query는 POPULAR 정렬과 server-side paging을 적용한다")
	void findPublicListingsWithLikes_sortsPopularAndPages() {
		Member seller = memberRepository.save(Member.create("seller2@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Member buyer1 = memberRepository.save(Member.create("buyer1@test.com", "Name", "Pass", "Nick1", "LOCAL"));
		Member buyer2 = memberRepository.save(Member.create("buyer2@test.com", "Name", "Pass", "Nick2", "LOCAL"));
		Item first = itemRepository.save(createLocatedItem(seller, "First", "Desc", 1000L, "A", "A"), List.of());
		Item popular = itemRepository.save(createLocatedItem(seller, "Popular", "Desc", 1000L, "A", "A"), List.of());
		Item second = itemRepository.save(createLocatedItem(seller, "Second", "Desc", 1000L, "A", "A"), List.of());
		bookmarkRepository.save(Bookmark.create(buyer1, popular));
		bookmarkRepository.save(Bookmark.create(buyer2, popular));
		bookmarkRepository.save(Bookmark.create(seller, first));

		ItemListQuery firstPage = new ItemListQuery(null, null, ItemStatus.ON_SALE, ItemListSort.POPULAR, 0, 2);
		ItemListQuery secondPage = new ItemListQuery(null, null, ItemStatus.ON_SALE, ItemListSort.POPULAR, 1, 2);

		assertThat(extractItems(itemRepository.findPublicListingsWithLikes(firstPage))).containsExactly(popular,
				second);
		assertThat(extractItems(itemRepository.findPublicListingsWithLikes(secondPage))).containsExactly(first);
	}

	@Test
	@DisplayName("소유자 관리 목록은 public query 기본값으로 status/page를 제한하지 않는다")
	void findOwnerInventoryWithLikes_keepsFullOwnerInventory() {
		Member seller = memberRepository.save(Member.create("owner@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item sold = itemRepository.save(createLocatedItem(seller, "Sold", "Desc", 1000L, "A", "A"), List.of());
		Item reserved = itemRepository.save(createLocatedItem(seller, "Reserved", "Desc", 1000L, "A", "A"), List.of());
		ReflectionTestUtils.setField(sold, "status", ItemStatus.SOLD);
		ReflectionTestUtils.setField(reserved, "status", ItemStatus.RESERVED);
		for (int index = 0; index < 21; index++) {
			itemRepository.save(createLocatedItem(seller, "Item " + index, "Desc", 1000L, "A", "A"), List.of());
		}

		List<Item> items = extractItems(itemRepository.findOwnerInventoryWithLikes(seller.getId()));

		assertThat(items).hasSize(23);
		assertThat(items).contains(sold, reserved);
	}

	@Test
	@DisplayName("관심 수 집계는 legacy seller self-interest row를 제외한다")
	void likesCount_excludesSellerSelfInterestRows() {
		Member seller = memberRepository.save(Member.create("self-seller@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Member buyer = memberRepository.save(Member.create("self-buyer@test.com", "Name", "Pass", "Buyer", "LOCAL"));
		Item item = itemRepository.save(createLocatedItem(seller, "Self", "Desc", 1000L, "A", "A"), List.of());
		bookmarkRepository.save(Bookmark.create(seller, item));
		bookmarkRepository.save(Bookmark.create(buyer, item));

		ItemListQuery query = new ItemListQuery(null, null, ItemStatus.ON_SALE, ItemListSort.LATEST, 0, 20);

		assertThat(extractLikes(itemRepository.findPublicListingsWithLikes(query), item)).isEqualTo(1L);
		assertThat(extractLikes(itemRepository.findOwnerInventoryWithLikes(seller.getId()), item)).isEqualTo(1L);
	}

	@Test
	@DisplayName("접근 가능한 목록은 내 상품과 초대받은 판매자 상품만 반환한다")
	void findAccessibleListingsWithLikes_filtersByAccessMembership() {
		Member seller = memberRepository
			.save(Member.create("access-seller@test.com", "Name", "Pass", "Seller", "LOCAL"));
		Member buyer = memberRepository.save(Member.create("access-buyer@test.com", "Name", "Pass", "Buyer", "LOCAL"));
		Member stranger = memberRepository
			.save(Member.create("access-stranger@test.com", "Name", "Pass", "Stranger", "LOCAL"));
		Item sellerItem = itemRepository
			.save(createLocatedItem(seller, "Shared Camera", "Desc", 1000L, "Gangnam", "Seoul"), List.of());
		Item buyerItem = itemRepository.save(createLocatedItem(buyer, "My Camera", "Desc", 1000L, "Gangnam", "Seoul"),
				List.of());
		Item strangerItem = itemRepository
			.save(createLocatedItem(stranger, "Hidden Camera", "Desc", 1000L, "Gangnam", "Seoul"), List.of());
		SellerShareLink shareLink = sellerShareLinkRepository
			.save(SellerShareLink.create(seller, "access-token", LocalDateTime.now().plusDays(1)));
		sellerAccessMemberRepository.save(SellerAccessMember.create(seller, buyer, shareLink));
		bookmarkRepository.save(Bookmark.create(stranger, sellerItem));

		ItemListQuery query = new ItemListQuery("camera", "gangnam", ItemStatus.ON_SALE, ItemListSort.POPULAR, 0, 20);

		List<Item> items = extractItems(itemRepository.findAccessibleListingsWithLikes(buyer.getId(), query));
		assertThat(items).containsExactly(sellerItem, buyerItem);
		assertThat(items).doesNotContain(strangerItem);
	}

	@Test
	@DisplayName("판매자 접근 멤버 저장 후 활성 상태로 조회된다")
	void sellerAccessMember_saveAndFindActive() {
		Member seller = memberRepository
			.save(Member.create("member-seller@test.com", "Name", "Pass", "Seller", "LOCAL"));
		Member member = memberRepository.save(Member.create("member-buyer@test.com", "Name", "Pass", "Buyer", "LOCAL"));
		SellerShareLink shareLink = sellerShareLinkRepository
			.save(SellerShareLink.create(seller, "member-token", LocalDateTime.now().plusDays(1)));

		SellerAccessMember saved = sellerAccessMemberRepository
			.save(SellerAccessMember.create(seller, member, shareLink));

		assertThat(saved.isActive()).isTrue();
		assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
		assertThat(saved.getMember().getId()).isEqualTo(member.getId());
		assertThat(saved.getSourceShareLink().getToken()).isEqualTo("member-token");
		assertThat(sellerAccessMemberRepository.findBySellerIdAndMemberId(seller.getId(), member.getId()))
			.contains(saved);
		assertThat(sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(seller.getId(), member.getId()))
			.isTrue();
	}

	private Item createLocatedItem(Member member, String title, String description, Long price, String locationName,
			String locationAddress) {
		Item item = Item.create(member, title, description, price, ItemType.SELL);
		item.updateTradeLocation(locationName, locationAddress, 37.0, 127.0);
		return item;
	}

	private List<Item> extractItems(List<Object[]> rows) {
		return rows.stream().map(row -> (Item) row[0]).toList();
	}

	private Long extractLikes(List<Object[]> rows, Item item) {
		return rows.stream()
			.filter(row -> ((Item) row[0]).getId().equals(item.getId()))
			.map(row -> (Long) row[2])
			.findFirst()
			.orElseThrow();
	}

}
