package com.knock.storage.db.core.review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

	Review save(Review review);

	Optional<Review> findById(Long id);

	List<Review> findByRevieweeId(Long revieweeId);

	Long countReviewByUserId(Long userId);

	boolean existsByReservationIdAndReviewerId(Long reservationId, Long memberId);

}
