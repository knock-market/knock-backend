package com.knock.core.domain.bookmark;

import com.knock.core.domain.bookmark.dto.BookmarkResult;
import com.knock.core.domain.bookmark.dto.BookmarkToggleData;
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
public class BookmarkService {

	// 리팩토링 필요
	private final BookmarkRepository bookmarkRepository;

	private final MemberRepository memberRepository;

	private final ItemRepository itemRepository;

	@Transactional
	public boolean toggleBookmark(Long memberId, BookmarkToggleData data) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		Item item = itemRepository.findById(data.itemId())
			.orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		// 현재 북마크 상태 반환
		return bookmarkRepository.findByMemberAndItemWithDeleted(memberId, data.itemId()).map(bookmark -> {
			if (bookmark.getDeletedAt() != null) { // 삭제된 북마크 복구
				bookmark.restore();
				return true;
			}
			// 기존 북마크 삭제
			bookmarkRepository.delete(bookmark);
			return false;
		}).orElseGet(() -> {
			Bookmark bookmark = Bookmark.create(member, item);
			bookmarkRepository.save(bookmark);
			return true;
		});
	}

	@Transactional(readOnly = true)
	public List<BookmarkResult> getMyBookmarks(Long memberId) {
		List<Bookmark> bookmarks = bookmarkRepository.findAllByMemberIdJoined(memberId);

		return bookmarks.stream().map(bookmark -> {
			Item item = bookmark.getItem();
			List<ItemImage> images = item.getImages();
			String thumbnailUrl = images.isEmpty() ? null : images.getFirst().getImageUrl();
			return BookmarkResult.from(bookmark, thumbnailUrl);
		}).toList();
	}

}
