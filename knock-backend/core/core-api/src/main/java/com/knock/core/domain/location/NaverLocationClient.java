package com.knock.core.domain.location;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;

@Component
public class NaverLocationClient {

	private static final String LOCAL_SEARCH_ENDPOINT = "https://openapi.naver.com/v1/search/local.json";

	private static final String GEOCODE_ENDPOINT = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode";

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);

	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final RestClient restClient;

	public NaverLocationClient(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.requestFactory(createRequestFactory())
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				throw new RestClientResponseException("Naver location request failed", response.getStatusCode().value(),
						response.getStatusText(), response.getHeaders(), response.getBody().readAllBytes(), null);
			})
			.build();
	}

	public NaverLocalSearchResponse searchLocal(String query, int display, NaverLocationProperties properties) {
		return restClient.get()
			.uri(LOCAL_SEARCH_ENDPOINT + "?query={query}&display={display}&start=1&sort=random", query, display)
			.header("X-Naver-Client-Id", properties.getSearchClientId())
			.header("X-Naver-Client-Secret", properties.getSearchClientSecret())
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(NaverLocalSearchResponse.class);
	}

	public NaverGeocodeResponse geocode(String query, NaverLocationProperties properties) {
		return restClient.get()
			.uri(GEOCODE_ENDPOINT + "?query={query}", query)
			.header("x-ncp-apigw-api-key-id", properties.getMapClientId())
			.header("x-ncp-apigw-api-key", properties.getMapClientSecret())
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(NaverGeocodeResponse.class);
	}

	public record NaverLocalSearchResponse(List<NaverLocalPlace> items) {
	}

	public record NaverLocalPlace(String title, String category, String address, String roadAddress, String mapx,
			String mapy) {
	}

	public record NaverGeocodeResponse(List<NaverAddress> addresses) {
	}

	public record NaverAddress(String roadAddress, String jibunAddress, String x, String y) {
	}

	private SimpleClientHttpRequestFactory createRequestFactory() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		return requestFactory;
	}

}
