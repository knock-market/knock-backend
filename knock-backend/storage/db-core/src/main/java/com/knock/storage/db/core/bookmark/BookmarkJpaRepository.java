package com.knock.storage.db.core.bookmark;

import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface BookmarkJpaRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByMemberAndItem(Member member, Item item);

    boolean existsByMemberAndItem(Member member, Item item);

    @Query(value = "SELECT * FROM bookmark WHERE member_id = :memberId AND item_id = :itemId", nativeQuery = true)
    Optional<Bookmark> findByMemberAndItemWithDeleted(Long memberId, Long itemId);

    List<Bookmark> findByMemberId(Long memberId);

}
