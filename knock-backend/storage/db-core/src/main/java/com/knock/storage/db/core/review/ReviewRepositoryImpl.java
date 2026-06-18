package com.knock.storage.db.core.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

	private final ReviewJpaRepository jpaRepository;

	@Override
	public Review save(Review review) {
		return jpaRepository.save(review);
	}

	@Override
	public Optional<Review> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<Review> findByRevieweeId(Long revieweeId) {
		return jpaRepository.findByRevieweeId(revieweeId);
	}

	@Override
	public Long countReviewByUserId(Long userId) {
		return jpaRepository.countByReviewee_Id(userId);
	}

	@Override
	public boolean existsByReservationIdAndReviewerId(Long reservationId, Long reviewerId) {
		return jpaRepository.existsByReservation_IdAndReviewer_Id(reservationId, reviewerId);
	}

}
