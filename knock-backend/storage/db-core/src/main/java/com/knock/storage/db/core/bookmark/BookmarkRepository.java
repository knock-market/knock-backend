package com.knock.storage.db.core.bookmark;

import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {

	Bookmark save(Bookmark bookmark);

	Optional<Bookmark> findById(Long id);

	void delete(Bookmark bookmark);

	Optional<Bookmark> findByMemberAndItem(Member member, Item item);

	boolean existsByMemberAndItem(Member member, Item item);

	Optional<Bookmark> findByMemberAndItemWithDeleted(Long memberId, Long itemId);

	List<Bookmark> findByMemberId(Long memberId);

	List<Bookmark> findAllByMemberIdJoined(Long memberId);

}
