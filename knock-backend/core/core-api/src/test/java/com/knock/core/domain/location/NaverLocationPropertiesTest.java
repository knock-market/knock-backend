package com.knock.core.domain.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaverLocationPropertiesTest {

	@Test
	@DisplayName("지도와 지역 검색 키가 모두 있으면 전체 설정 완료로 판단한다")
	void isConfigured_success_allCredentials() {
		// given
		NaverLocationProperties properties = new NaverLocationProperties("map-id", "map-secret", "search-id",
				"search-secret");

		// when & then
		assertThat(properties.isConfigured()).isTrue();
		assertThat(properties.isMapConfigured()).isTrue();
		assertThat(properties.isSearchConfigured()).isTrue();
	}

	@Test
	@DisplayName("지도 키만 있어도 주소 검색은 설정 완료로 판단한다")
	void isConfigured_success_mapCredentialsOnly() {
		// given
		NaverLocationProperties properties = new NaverLocationProperties("map-id", "map-secret", "", "");

		// when & then
		assertThat(properties.isConfigured()).isTrue();
		assertThat(properties.isMapConfigured()).isTrue();
		assertThat(properties.isSearchConfigured()).isFalse();
	}

	@Test
	@DisplayName("지역 검색 키만 있어도 장소명 검색은 설정 완료로 판단한다")
	void isConfigured_success_searchCredentialsOnly() {
		// given
		NaverLocationProperties properties = new NaverLocationProperties("", "", "search-id", "search-secret");

		// when & then
		assertThat(properties.isConfigured()).isTrue();
		assertThat(properties.isMapConfigured()).isFalse();
		assertThat(properties.isSearchConfigured()).isTrue();
	}

	@Test
	@DisplayName("빈 키가 하나라도 있으면 해당 네이버 기능은 미설정으로 판단한다")
	void isConfigured_fail_blankPartialCredentials() {
		// given
		NaverLocationProperties properties = new NaverLocationProperties("map-id", " ", "search-id", "");

		// when & then
		assertThat(properties.isConfigured()).isFalse();
		assertThat(properties.isMapConfigured()).isFalse();
		assertThat(properties.isSearchConfigured()).isFalse();
	}

}
