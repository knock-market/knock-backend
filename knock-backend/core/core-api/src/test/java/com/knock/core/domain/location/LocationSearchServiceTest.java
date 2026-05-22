package com.knock.core.domain.location;

import com.knock.core.domain.location.NaverLocationClient.NaverAddress;
import com.knock.core.domain.location.NaverLocationClient.NaverGeocodeResponse;
import com.knock.core.domain.location.NaverLocationClient.NaverLocalPlace;
import com.knock.core.domain.location.NaverLocationClient.NaverLocalSearchResponse;
import com.knock.core.domain.location.dto.LocationSearchResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LocationSearchServiceTest {

	private final NaverLocationClient naverLocationClient = mock(NaverLocationClient.class);

	private final NaverLocationProperties properties = mock(NaverLocationProperties.class);

	private final LocationSearchService locationSearchService = new LocationSearchService(naverLocationClient,
			properties);

	@Test
	@DisplayName("장소명 검색 성공")
	void search_placeName_success() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(true);
		given(properties.isMapConfigured()).willReturn(true);
		given(naverLocationClient.searchLocal("성수역", 10, properties))
			.willReturn(new NaverLocalSearchResponse(List.of(new NaverLocalPlace("<b>성수역</b>", "교통,수송>지하철",
					"서울 성동구 성수동2가", "서울 성동구 아차산로 100", "1270560000", "375440000"))));
		given(naverLocationClient.geocode("서울 성동구 아차산로 100", properties)).willReturn(new NaverGeocodeResponse(
				List.of(new NaverAddress("서울 성동구 아차산로 100", "서울 성동구 성수동2가", "127.056", "37.544"))));

		// when
		List<LocationSearchResult> results = locationSearchService.search("성수역");

		// then
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().name()).isEqualTo("성수역");
		assertThat(results.getFirst().latitude()).isEqualTo(37.544);
		assertThat(results.getFirst().longitude()).isEqualTo(127.056);
		assertThat(results.getFirst().naverMapX()).isEqualTo(1270560000.0);
	}

	@Test
	@DisplayName("지역 검색 좌표가 있으면 추가 Geocoding 호출 없이 반환")
	void search_placeName_skipsGeocodeWhenLocalCoordinatesExist() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(true);
		given(naverLocationClient.searchLocal("죽전역", 10, properties))
			.willReturn(new NaverLocalSearchResponse(List.of(new NaverLocalPlace("<b>죽전역</b>", "교통,수송>지하철",
					"경기 용인시 수지구 죽전동", "경기 용인시 수지구 포은대로 530", "1271073950", "373247530"))));

		// when
		List<LocationSearchResult> results = locationSearchService.search("죽전역");

		// then
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().latitude()).isEqualTo(37.324753);
		assertThat(results.getFirst().longitude()).isEqualTo(127.107395);
		verify(naverLocationClient, never()).geocode(any(), any());
	}

	@Test
	@DisplayName("지역 검색 결과가 없으면 주소 검색으로 보강")
	void search_fallbackToGeocode_success() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(true);
		given(properties.isMapConfigured()).willReturn(true);
		given(naverLocationClient.searchLocal("서울 성동구 아차산로 100", 10, properties))
			.willReturn(new NaverLocalSearchResponse(List.of()));
		given(naverLocationClient.geocode("서울 성동구 아차산로 100", properties)).willReturn(new NaverGeocodeResponse(
				List.of(new NaverAddress("서울 성동구 아차산로 100", "서울 성동구 성수동2가", "127.056", "37.544"))));

		// when
		List<LocationSearchResult> results = locationSearchService.search(" 서울 성동구 아차산로 100 ");

		// then
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().address()).isEqualTo("서울 성동구 아차산로 100");
		assertThat(results.getFirst().naverMapX()).isNull();
	}

	@Test
	@DisplayName("주소 검색은 지역 검색 키가 없어도 Geocoding으로 처리")
	void search_addressWithoutSearchKey_success() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(false);
		given(properties.isMapConfigured()).willReturn(true);
		given(naverLocationClient.geocode("서울 성동구 아차산로 100", properties)).willReturn(new NaverGeocodeResponse(
				List.of(new NaverAddress("서울 성동구 아차산로 100", "서울 성동구 성수동2가", "127.056", "37.544"))));

		// when
		List<LocationSearchResult> results = locationSearchService.search("서울 성동구 아차산로 100");

		// then
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().latitude()).isEqualTo(37.544);
	}

	@Test
	@DisplayName("지역 검색 결과는 주소 좌표 변환이 실패해도 반환")
	void search_placeName_success_withoutGeocode() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(true);
		given(properties.isMapConfigured()).willReturn(true);
		given(naverLocationClient.searchLocal("죽전역", 10, properties))
			.willReturn(new NaverLocalSearchResponse(List.of(new NaverLocalPlace("<b>죽전역</b>", "교통,수송>지하철",
					"경기 용인시 수지구 죽전동", "경기 용인시 수지구 포은대로 530", "1271073950", "373247530"))));
		given(naverLocationClient.geocode("경기 용인시 수지구 포은대로 530", properties))
			.willThrow(new RestClientException("geocode disabled"));

		// when
		List<LocationSearchResult> results = locationSearchService.search("죽전역");

		// then
		assertThat(results).hasSize(1);
		assertThat(results.getFirst().name()).isEqualTo("죽전역");
		assertThat(results.getFirst().latitude()).isEqualTo(37.324753);
		assertThat(results.getFirst().longitude()).isEqualTo(127.107395);
		assertThat(results.getFirst().naverMapX()).isEqualTo(1271073950.0);
	}

	@Test
	@DisplayName("실패 - 설정 없음")
	void search_fail_notConfigured() {
		// given
		given(properties.isConfigured()).willReturn(false);

		// when & then
		assertThatThrownBy(() -> locationSearchService.search("성수역")).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.LOCATION_SEARCH_UNAVAILABLE);
	}

	@Test
	@DisplayName("실패 - 장소명 검색 키 없음")
	void search_fail_placeSearchNotConfigured() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(false);

		// when & then
		assertThatThrownBy(() -> locationSearchService.search("죽전역")).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.LOCATION_SEARCH_UNAVAILABLE);
	}

	@Test
	@DisplayName("외부 API 오류는 빈 결과로 처리")
	void search_empty_externalApiError() {
		// given
		given(properties.isConfigured()).willReturn(true);
		given(properties.isSearchConfigured()).willReturn(true);
		given(properties.isMapConfigured()).willReturn(true);
		given(naverLocationClient.searchLocal("성수역", 10, properties)).willThrow(new RestClientException("naver error"));
		given(naverLocationClient.geocode("성수역", properties)).willThrow(new RestClientException("naver error"));

		// when
		List<LocationSearchResult> results = locationSearchService.search("성수역");

		// then
		assertThat(results).isEmpty();
	}

}
