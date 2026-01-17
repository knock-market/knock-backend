package com.knock.storage.db.core.bookmark;

import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {

    Bookmark save(Bookmark bookmark);

    void delete(Bookmark bookmark);

    Optional<Bookmark> findByMemberAndItem(Member member, Item item);

    List<Bookmark> findByMemberId(Long memberId);

}
