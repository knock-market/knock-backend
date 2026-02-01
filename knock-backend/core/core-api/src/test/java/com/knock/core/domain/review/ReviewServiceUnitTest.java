package com.knock.core.domain.review;

import com.knock.core.domain.review.dto.request.ReviewCreateData;
import com.knock.core.domain.review.dto.response.ReviewResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.review.Review;
import com.knock.storage.db.core.review.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {

	@InjectMocks
	private ReviewService reviewService;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ReputationService reputationService;

	@Test
	@DisplayName("리뷰 작성 성공: 예약이 확인되면 리뷰가 저장되고 신뢰도가 업데이트된다.")
	void createReview_Success() {
		// given
		Long reviewerId = 1L;
		Long itemId = 100L;
		Long sellerId = 2L;
		int score = 5;
		String content = "아주 좋아요!";

		ReviewCreateData requestDto = new ReviewCreateData(itemId, content, score);

		// Mock 객체 생성 (Entity 내부 생성자가 protected라 Mocking 활용)
		Reservation mockReservation = mock(Reservation.class);
		Item mockItem = mock(Item.class);
		Member mockSeller = mock(Member.class);
		Member mockBuyer = mock(Member.class);
		Review mockSavedReview = mock(Review.class);

		// Stubbing: 예약 조회 시 연관된 객체들이 잘 반환되도록 설정
		given(reservationRepository.findReservationForReview(reviewerId, itemId))
			.willReturn(Optional.of(mockReservation));

		given(mockReservation.getItem()).willReturn(mockItem);
		given(mockReservation.getMember()).willReturn(mockBuyer); // 구매자
		given(mockItem.getMember()).willReturn(mockSeller); // 판매자

		// Stubbing: 리뷰 저장 시 저장된 리뷰 반환 설정
		given(reviewRepository.save(any(Review.class))).willReturn(mockSavedReview);

		// Stubbing: 반환값(ReviewResult) 생성을 위한 Mock 설정
		given(mockSavedReview.getId()).willReturn(10L);
		given(mockSavedReview.getContent()).willReturn(content);
		given(mockSavedReview.getScore()).willReturn(score);

		// when
		ReviewResult result = reviewService.createReview(reviewerId, requestDto);

		// then
		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(content);

		// 1. 리뷰가 저장소에 저장되었는지 검증
		verify(reviewRepository).save(any(Review.class));

		// 2. 판매자의 신뢰도(Reputation) 업데이트 메서드가 호출되었는지 검증
		verify(reputationService).changeReputation(mockSeller, score);
	}

	@Test
	@DisplayName("리뷰 작성 실패: 예약을 찾을 수 없거나 완료되지 않은 경우 예외가 발생한다.")
	void createReview_Fail_NotFoundReservation() {
		// given
		Long reviewerId = 1L;
		ReviewCreateData requestDto = new ReviewCreateData(100L, "내용", 5);

		// Stubbing: 예약 조회 시 Empty 반환 (찾지 못함)
		given(reservationRepository.findReservationForReview(reviewerId, requestDto.itemId()))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(reviewerId, requestDto)).isInstanceOf(CoreException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.RESERVATION_NOT_COMPLETED);
	}

}