package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupRepository;
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

	@Autowired
	private GroupRepository groupRepository;

	@Test
	@DisplayName("상품 저장 및 조회 성공")
	void saveAndFindItem() {
		// given
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Group group = groupRepository.save(Group.create("Group", "Desc", member));
		Item item = Item.create("Title", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member);

		// when
		Item savedItem = itemRepository.save(item, List.of("http://image.url"));
		Optional<Item> foundItem = itemRepository.findById(savedItem.getId());

		// then
		assertThat(foundItem).isPresent();
		assertThat(foundItem.get().getTitle()).isEqualTo("Title");
	}

	@Test
	@DisplayName("그룹별 상품 목록 조회")
	void findByGroupId() {
		// given
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Group group = groupRepository.save(Group.create("Group", "Desc", member));
		itemRepository.save(
				Item.create("Item 1", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member),
				List.of());
		itemRepository.save(
				Item.create("Item 2", "Desc", 2000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member),
				List.of());

		// when
		List<Object[]> items = itemRepository.findByGroupIdWithLikes(group.getId());

		// then
		assertThat(items).hasSize(2);
	}

	@Test
	@DisplayName("상품 거래 위치 저장 및 조회 성공")
	void saveAndFindTradeLocation() {
		// given
		Member member = memberRepository.save(Member.create("location@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Group group = groupRepository.save(Group.create("Location Group", "Desc", member));
		Item item = Item.create("Location Item", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group,
				member);
		item.updateTradeLocation("학생회관", "서울특별시 성북구 안암로 145", 37.589387, 127.032477);

		// when
		Item savedItem = itemRepository.save(item, List.of());
		Optional<Item> foundItem = itemRepository.findById(savedItem.getId());

		// then
		assertThat(foundItem).isPresent();
		assertThat(foundItem.get().getTradeLocationName()).isEqualTo("학생회관");
		assertThat(foundItem.get().getTradeLocationAddress()).isEqualTo("서울특별시 성북구 안암로 145");
		assertThat(foundItem.get().getTradeLatitude()).isEqualTo(37.589387);
		assertThat(foundItem.get().getTradeLongitude()).isEqualTo(127.032477);
	}

}
