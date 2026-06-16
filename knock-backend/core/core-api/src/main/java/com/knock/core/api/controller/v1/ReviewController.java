package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.request.ReviewCreateRequest;
import com.knock.core.api.controller.v1.response.ReviewResponse;
import com.knock.core.domain.review.ReviewService;
import com.knock.core.domain.review.dto.request.ReviewCreateData;
import com.knock.core.domain.review.dto.response.ReviewResult;
import com.knock.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/reviews")
	public ApiResponse<ReviewResponse> createReview(@AuthenticationPrincipal MemberPrincipal principal,
			@Valid @RequestBody ReviewCreateRequest reviewCreateRequest) {
		ReviewCreateData data = new ReviewCreateData(reviewCreateRequest.itemId(), reviewCreateRequest.content(),
				reviewCreateRequest.score());
		ReviewResult reviewResult = reviewService.createReview(principal.getMemberId(), data);

		return ApiResponse.success(ReviewResponse.from(reviewResult));
	}

	@GetMapping("/members/{memberId}/reviews")
	public ApiResponse<List<ReviewResponse>> getReviewList(@PathVariable Long memberId) {
		List<ReviewResult> results = reviewService.getReviewList(memberId);
		List<ReviewResponse> response = results.stream().map(ReviewResponse::from).toList();

		return ApiResponse.success(response);
	}

}
