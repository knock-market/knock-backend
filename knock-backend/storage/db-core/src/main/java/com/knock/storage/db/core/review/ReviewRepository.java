package com.knock.storage.db.core.review;

import java.util.List;

public interface ReviewRepository {

    Review save(Review review);

    List<Review> findByRevieweeId(Long revieweeId);

    boolean existsByReservationId(Long reservationId);

}
