package com.knock.storage.db.core.bookmark;

import com.knock.core.enums.ItemType;
import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BookmarkRepositoryTest extends CoreDbContextTest {

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Test
	@DisplayName("북마크 저장 및 조회")
	void saveAndFindBookmark() {
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = itemRepository.save(Item.create(member, "Title", "Desc", 1000L, ItemType.SELL), List.of());
		Bookmark bookmark = Bookmark.create(member, item);

		Bookmark saved = bookmarkRepository.save(bookmark);
		Optional<Bookmark> found = bookmarkRepository.findById(saved.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getItem().getId()).isEqualTo(item.getId());
	}

	@Test
	@DisplayName("멤버별 북마크 목록 조회")
	void findAllByMemberIdWithItemAndImages() {
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = itemRepository.save(Item.create(member, "Title", "Desc", 1000L, ItemType.SELL), List.of());
		bookmarkRepository.save(Bookmark.create(member, item));

		List<Bookmark> bookmarks = bookmarkRepository.findAllByMemberIdJoined(member.getId());

		assertThat(bookmarks).hasSize(1);
		assertThat(bookmarks.get(0).getItem().getTitle()).isEqualTo("Title");
	}

}
