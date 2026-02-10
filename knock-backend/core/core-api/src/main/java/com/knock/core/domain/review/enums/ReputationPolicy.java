package com.knock.core.domain.review.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ReputationPolicy {

	BEST(5, 1.0), GOOD(4, 0.5), NORMAL(3, 0.0), BAD(2, -2.0), WORST(1, -5.0);

	private final int reviewScore;

	private final double reputationChange;

	public static double calculateChange(int score) {
		return Arrays.stream(values())
			.filter(p -> p.reviewScore == score)
			.findFirst()
			.map(ReputationPolicy::getReputationChange)
			.orElse(0.0);
	}

}