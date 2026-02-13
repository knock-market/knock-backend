package com.knock.storage.db.core.bookmark;

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
	public Optional<Bookmark> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public void delete(Bookmark bookmark) {
		jpaRepository.delete(bookmark);
	}

	@Override
	public Optional<Bookmark> findByMemberAndItemWithDeleted(Long memberId, Long itemId) {
		return jpaRepository.findByMemberAndItemWithDeleted(memberId, itemId);
	}

	@Override
	public List<Bookmark> findByMemberId(Long memberId) {
		return jpaRepository.findByMemberId(memberId);
	}

	@Override
	public List<Bookmark> findAllByMemberIdJoined(Long memberId) {
		return jpaRepository.findAllByMemberIdJoined(memberId);
	}

}
