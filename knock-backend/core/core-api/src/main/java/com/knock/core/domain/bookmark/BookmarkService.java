package com.knock.core.domain.bookmark;

import com.knock.core.api.controller.v1.response.BookmarkResponseDto;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.bookmark.BookmarkRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemImage;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void toggleBookmark(Long memberId, Long itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

        bookmarkRepository.findByMemberAndItemWithDeleted(memberId, itemId)
                .ifPresentOrElse(bookmark -> {
                    if (bookmark.getDeletedAt() != null) {
                        bookmark.restore();
                    } else {
                        bookmarkRepository.delete(bookmark);
                    }
                }, () -> {
                    Bookmark bookmark = Bookmark.create(member, item);
                    bookmarkRepository.save(bookmark);
                });
    }

    public List<BookmarkResponseDto> getMyBookmarks(Long memberId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberId(memberId);

        return bookmarks.stream().map(bookmark -> {
            Item item = bookmark.getItem();
            List<ItemImage> images = item.getImages();
            String thumbnailUrl = images.isEmpty() ? null : images.getFirst().getImageUrl();
            return BookmarkResponseDto.from(bookmark, thumbnailUrl);
        }).toList();
    }
}
