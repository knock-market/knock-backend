package com.knock.storage.db.core.bookmark;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.config.QueryDslConfig;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @DisplayName("북마크 저장 및 조회")
    void saveAndFindBookmark() {
        // given
        Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
        Group group = groupRepository.save(Group.create("Group", "Desc", member));
        Item item = itemRepository
                .save(Item.create("Title", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member));
        Bookmark bookmark = Bookmark.create(member, item);

        // when
        Bookmark saved = bookmarkRepository.save(bookmark);
        Optional<Bookmark> found = bookmarkRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("멤버별 북마크 목록 조회 (Fetch Join)")
    void findAllByMemberIdWithItemAndImages() {
        // given
        Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
        Group group = groupRepository.save(Group.create("Group", "Desc", member));
        Item item = itemRepository
                .save(Item.create("Title", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member));
        bookmarkRepository.save(Bookmark.create(member, item));

        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMemberIdJoined(member.getId());

        // then
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getItem().getTitle()).isEqualTo("Title");
    }
}
