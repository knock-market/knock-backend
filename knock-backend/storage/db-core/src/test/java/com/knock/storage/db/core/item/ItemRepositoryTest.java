package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemType;
import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTest extends CoreDbContextTest {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private MemberRepository memberRepository;

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

}
