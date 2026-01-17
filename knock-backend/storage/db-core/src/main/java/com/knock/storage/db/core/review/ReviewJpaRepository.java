package com.knock.storage.db.core.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeId(Long revieweeId);

    boolean existsByReservationId(Long reservationId);

}
