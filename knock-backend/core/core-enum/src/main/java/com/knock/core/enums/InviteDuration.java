package com.knock.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum InviteDuration {

	FIVE_MINUTES(Duration.ofMinutes(5)), THIRTY_MINUTES(Duration.ofMinutes(30)), ONE_HOUR(Duration.ofHours(1)),
	ONE_DAY(Duration.ofDays(1)), PERMANENT(null);

	private final Duration duration;

}
