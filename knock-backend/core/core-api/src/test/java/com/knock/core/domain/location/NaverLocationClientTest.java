package com.knock.core.domain.location;

import com.knock.core.domain.location.NaverLocationClient.NaverGeocodeResponse;
import com.knock.core.domain.location.NaverLocationClient.NaverLocalSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NaverLocationClientTest {

	private final RestClient.Builder restClientBuilder = RestClient.builder();

	private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

	private final NaverLocationClient naverLocationClient = new NaverLocationClient(restClientBuilder.build());

	private final NaverLocationProperties properties = new NaverLocationProperties("map-id", "map-secret", "search-id",
			"search-secret");

	@Test
	@DisplayName("지역 검색 요청은 Search API 인증 헤더와 함께 결과를 역직렬화한다")
	void searchLocal_success() {
		// given
		server.expect(requestTo(
				"https://openapi.naver.com/v1/search/local.json?query=%EC%84%B1%EC%88%98%EC%97%AD&display=5&start=1&sort=random"))
			.andExpect(method(org.springframework.http.HttpMethod.GET))
			.andExpect(header("X-Naver-Client-Id", "search-id"))
			.andExpect(header("X-Naver-Client-Secret", "search-secret"))
			.andRespond(withSuccess("""
					{
					  "items": [
					    {
					      "title": "<b>성수역</b>",
					      "category": "교통,수송>지하철",
					      "address": "서울 성동구 성수동2가",
					      "roadAddress": "서울 성동구 아차산로 100",
					      "mapx": "1270560000",
					      "mapy": "375440000"
					    }
					  ]
					}
					""", MediaType.APPLICATION_JSON));

		// when
		NaverLocalSearchResponse response = naverLocationClient.searchLocal("성수역", 5, properties);

		// then
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().getFirst().title()).isEqualTo("<b>성수역</b>");
		assertThat(response.items().getFirst().mapx()).isEqualTo("1270560000");
		server.verify();
	}

	@Test
	@DisplayName("주소 좌표 변환 요청은 Maps API 인증 헤더와 함께 결과를 역직렬화한다")
	void geocode_success() {
		// given
		server.expect(requestTo(
				"https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=%EC%84%9C%EC%9A%B8%20%EC%84%B1%EB%8F%99%EA%B5%AC%20%EC%95%84%EC%B0%A8%EC%82%B0%EB%A1%9C%20100"))
			.andExpect(method(org.springframework.http.HttpMethod.GET))
			.andExpect(header("x-ncp-apigw-api-key-id", "map-id"))
			.andExpect(header("x-ncp-apigw-api-key", "map-secret"))
			.andRespond(withSuccess("""
					{
					  "addresses": [
					    {
					      "roadAddress": "서울 성동구 아차산로 100",
					      "jibunAddress": "서울 성동구 성수동2가",
					      "x": "127.056",
					      "y": "37.544"
					    }
					  ]
					}
					""", MediaType.APPLICATION_JSON));

		// when
		NaverGeocodeResponse response = naverLocationClient.geocode("서울 성동구 아차산로 100", properties);

		// then
		assertThat(response.addresses()).hasSize(1);
		assertThat(response.addresses().getFirst().roadAddress()).isEqualTo("서울 성동구 아차산로 100");
		assertThat(response.addresses().getFirst().x()).isEqualTo("127.056");
		server.verify();
	}

	@Test
	@DisplayName("네이버 API 오류 응답은 RestClientResponseException으로 보고한다")
	void geocode_fail_errorStatus() {
		// given
		server.expect(requestTo(
				"https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=%EC%97%86%EB%8A%94%20%EC%A3%BC%EC%86%8C"))
			.andRespond(withResourceNotFound());

		// when & then
		assertThatThrownBy(() -> naverLocationClient.geocode("없는 주소", properties))
			.isInstanceOf(RestClientResponseException.class)
			.hasMessageContaining("404");
		server.verify();
	}

}
