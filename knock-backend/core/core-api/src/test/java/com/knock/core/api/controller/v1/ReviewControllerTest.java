package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.ReviewCreateRequest;
import com.knock.core.domain.review.ReviewService;
import com.knock.core.domain.review.dto.response.ReviewResult;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ReviewControllerTest extends RestDocsTest {

	private ReviewService reviewService;

	private ReviewController reviewController;

	private MemberPrincipal principal;

	@BeforeEach
	void setUp() {
		reviewService = mock(ReviewService.class);
		reviewController = new ReviewController(reviewService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(reviewController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("리뷰 작성 성공")
	void createReview_success() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(TEST_ITEM_ID, "좋은 거래였습니다.", 5);
		given(reviewService.createReview(anyLong(), any())).willReturn(new ReviewResult(10L, request.content(), 5));

		// when & then
		restDocGiven().contentType(ContentType.JSON).body(request).post("/api/v1/reviews").then().status(HttpStatus.OK);
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 점수 유효성 검증")
	void createReview_failValidation() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(TEST_ITEM_ID, "좋은 거래였습니다.", 6);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/reviews")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(reviewService);
	}

	@Test
	@DisplayName("회원 리뷰 목록 조회 성공")
	void getReviewList_success() {
		// given
		given(reviewService.getReviewList(TEST_MEMBER_ID)).willReturn(List.of(new ReviewResult(10L, "좋아요", 5)));

		// when & then
		restDocGiven().pathParam("memberId", TEST_MEMBER_ID)
			.get("/api/v1/members/{memberId}/reviews")
			.then()
			.status(HttpStatus.OK);
	}

}
