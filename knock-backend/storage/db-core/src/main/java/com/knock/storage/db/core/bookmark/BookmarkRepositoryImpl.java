package com.knock.storage.db.core.bookmark;

import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryImpl implements BookmarkRepository {

    private final BookmarkJpaRepository jpaRepository;

    @Override
    public Bookmark save(Bookmark bookmark) {
        return jpaRepository.save(bookmark);
    }

    @Override
    public void delete(Bookmark bookmark) {
        jpaRepository.delete(bookmark);
    }

    @Override
    public Optional<Bookmark> findByMemberAndItem(Member member, Item item) {
        return jpaRepository.findByMemberAndItem(member, item);
    }

    @Override
    public boolean existsByMemberAndItem(Member member, Item item) {
        return jpaRepository.existsByMemberAndItem(member, item);
    }

    @Override
    public Optional<Bookmark> findByMemberAndItemWithDeleted(Long memberId, Long itemId) {
        return jpaRepository.findByMemberAndItemWithDeleted(memberId, itemId);
    }

    @Override
    public List<Bookmark> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId);
    }

}
