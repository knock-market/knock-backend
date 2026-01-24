package com.knock.storage.db.core.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

	private final ReviewJpaRepository jpaRepository;

	@Override
	public Review save(Review review) {
		return jpaRepository.save(review);
	}

	@Override
	public List<Review> findByRevieweeId(Long revieweeId) {
		return jpaRepository.findByRevieweeId(revieweeId);
	}

	@Override
	public boolean existsByReservationId(Long reservationId) {
		return jpaRepository.existsByReservationId(reservationId);
	}

}
