package com.knock.core.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

	public static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Seoul");

	@Bean
	public Clock applicationClock() {
		return Clock.system(APPLICATION_ZONE);
	}

}
