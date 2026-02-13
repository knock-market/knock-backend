package com.knock.storage.db.core.bookmark;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {

	Bookmark save(Bookmark bookmark);

	Optional<Bookmark> findById(Long id);

	void delete(Bookmark bookmark);

	Optional<Bookmark> findByMemberAndItemWithDeleted(Long memberId, Long itemId);

	List<Bookmark> findByMemberId(Long memberId);

	List<Bookmark> findAllByMemberIdJoined(Long memberId);

}
