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
		List<Item> items = itemRepository.findByGroupId(group.getId());

		// then
		assertThat(items).hasSize(2);
	}

}
