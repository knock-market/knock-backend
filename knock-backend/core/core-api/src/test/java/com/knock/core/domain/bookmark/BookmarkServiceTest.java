package com.knock.core.domain.bookmark;

import com.knock.core.domain.bookmark.dto.BookmarkResult;
import com.knock.core.domain.bookmark.dto.BookmarkToggleData;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.bookmark.BookmarkRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkService bookmarkService;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ItemRepository itemRepository;

    @Nested
    @DisplayName("북마크 토글")
    class ToggleBookmark {

        @Test
        @DisplayName("새 북마크 생성")
        void createNewBookmark() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, createGroup(), member);
            BookmarkToggleData data = new BookmarkToggleData(TEST_ITEM_ID);

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
            given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
            given(bookmarkRepository.findByMemberAndItemWithDeleted(TEST_MEMBER_ID, TEST_ITEM_ID))
                    .willReturn(Optional.empty());

            // when
            boolean result = bookmarkService.toggleBookmark(TEST_MEMBER_ID, data);

            // then
            assertThat(result).isTrue();
            verify(bookmarkRepository).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("기존 북마크 삭제")
        void deleteExistingBookmark() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, createGroup(), member);
            Bookmark bookmark = createBookmark(TEST_BOOKMARK_ID, member, item);
            BookmarkToggleData data = new BookmarkToggleData(TEST_ITEM_ID);

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
            given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
            given(bookmarkRepository.findByMemberAndItemWithDeleted(TEST_MEMBER_ID, TEST_ITEM_ID))
                    .willReturn(Optional.of(bookmark));

            // when
            boolean result = bookmarkService.toggleBookmark(TEST_MEMBER_ID, data);

            // then
            assertThat(result).isFalse();
            verify(bookmarkRepository).delete(bookmark);
        }

        @Test
        @DisplayName("삭제된 북마크 복구")
        void restoreDeletedBookmark() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, createGroup(), member);
            Bookmark bookmark = createBookmark(TEST_BOOKMARK_ID, member, item);
            ReflectionTestUtils.setField(bookmark, "deletedAt", LocalDateTime.now().minusDays(1));
            BookmarkToggleData data = new BookmarkToggleData(TEST_ITEM_ID);

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
            given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
            given(bookmarkRepository.findByMemberAndItemWithDeleted(TEST_MEMBER_ID, TEST_ITEM_ID))
                    .willReturn(Optional.of(bookmark));

            // when
            boolean result = bookmarkService.toggleBookmark(TEST_MEMBER_ID, data);

            // then
            assertThat(result).isTrue();
            assertThat(bookmark.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("실패 - 회원 없음")
        void fail_memberNotFound() {
            // given
            BookmarkToggleData data = new BookmarkToggleData(TEST_ITEM_ID);
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.toggleBookmark(TEST_MEMBER_ID, data))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 상품 없음")
        void fail_itemNotFound() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            BookmarkToggleData data = new BookmarkToggleData(TEST_ITEM_ID);

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
            given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookmarkService.toggleBookmark(TEST_MEMBER_ID, data))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내 북마크 조회")
    class GetMyBookmarks {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, createGroup(), member);
            Bookmark bookmark = createBookmark(TEST_BOOKMARK_ID, member, item);

            given(bookmarkRepository.findAllByMemberIdJoined(TEST_MEMBER_ID))
                    .willReturn(List.of(bookmark));

            // when
            List<BookmarkResult> results = bookmarkService.getMyBookmarks(TEST_MEMBER_ID);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).itemId()).isEqualTo(TEST_ITEM_ID);
        }

        @Test
        @DisplayName("빈 목록")
        void emptyList() {
            // given
            given(bookmarkRepository.findAllByMemberIdJoined(TEST_MEMBER_ID))
                    .willReturn(List.of());

            // when
            List<BookmarkResult> results = bookmarkService.getMyBookmarks(TEST_MEMBER_ID);

            // then
            assertThat(results).isEmpty();
        }
    }
}
