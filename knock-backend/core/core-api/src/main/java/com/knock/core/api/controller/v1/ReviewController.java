package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ReviewCreateRequest;
import com.knock.core.api.controller.v1.response.ReviewResponse;
import com.knock.core.domain.review.ReviewService;
import com.knock.core.domain.review.dto.request.ReviewCreateData;
import com.knock.core.domain.review.dto.response.ReviewResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController("/api/v1/")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/reviews")
	public ApiResponse<ReviewResponse> createReview(@AuthenticationPrincipal MemberPrincipal principal,
			@RequestBody ReviewCreateRequest reviewCreateRequest) {
		ReviewResult reviewResult = reviewService.createReview(principal.getMemberId(),
				ReviewCreateData.from(reviewCreateRequest));

		return ApiResponse.success(ReviewResponse.from(reviewResult));
	}

	@GetMapping("/members/{memberId}/reviews")
	public ApiResponse<List<ReviewResponse>> getReviewList(@PathVariable Long memberId) {
		List<ReviewResponse> reviewList = reviewService.getReviewList(memberId);

		return ApiResponse.success(reviewList);
	}

}
