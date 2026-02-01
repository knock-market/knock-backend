package com.knock.storage.db.core.review;

import java.util.List;

public interface ReviewRepository {

	Review save(Review review);

	List<Review> findByRevieweeId(Long revieweeId);

	Long countReviewByUserId(Long userId);

	boolean existsByReservationIdAndReviewerId(Long reservationId, Long memberId);

}
