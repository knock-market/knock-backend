package com.knock.core.domain.review;

import com.knock.core.api.controller.v1.response.ReviewResponse;
import com.knock.core.domain.review.dto.request.ReviewCreateData;
import com.knock.core.domain.review.dto.response.ReviewResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.review.Review;
import com.knock.storage.db.core.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final MemberRepository memberRepository;

	private final ReviewRepository reviewRepository;

	private final ReservationRepository reservationRepository;

	private final ReputationService reputationService;

	@Transactional
	public ReviewResult createReview(Long memberId, ReviewCreateData reviewCreateData) {
		Reservation reservation = findReservation(memberId, reviewCreateData.itemId());
		validateDuplicateReview(reservation.getId(), memberId);

		Member buyer = reservation.getMember();
		Member seller = reservation.getItem().getMember();

		try {
			Review savedReview = reviewRepository
				.save(Review.create(reservation, buyer, seller, reviewCreateData.content(), reviewCreateData.score()));

			reputationService.changeReputation(seller, reviewCreateData.score());

			return ReviewResult.from(savedReview);
		}
		catch (DataIntegrityViolationException e) {

			throw new CoreException(ErrorType.DUPLICATE_REVIEW);
		}
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> getReviewList(Long writerId) {
		validateMemberExists(writerId);
		List<Review> reviewList = reviewRepository.findByRevieweeId(writerId);

		return reviewList.stream().map(ReviewResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public Long countReview(Long userId) {
		validateMemberExists(userId);

		return reviewRepository.countReviewByUserId(userId);
	}

	private void validateDuplicateReview(Long reservationId, Long reviewerId) {
		if (reviewRepository.existsByReservationIdAndReviewerId(reservationId, reviewerId)) {
			throw new CoreException(ErrorType.DUPLICATE_REVIEW);
		}
	}

	private void validateMemberExists(Long userId) {
		if (!memberRepository.existsById(userId)) {
			throw new CoreException(ErrorType.MEMBER_NOT_FOUND);
		}
	}

	private Reservation findReservation(Long reviewId, Long itemId) {
		return reservationRepository.findReservationForReview(reviewId, itemId)
			.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_COMPLETED));
	}

}
