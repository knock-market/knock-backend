package com.knock.core.domain.location;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NaverLocationProperties {

	private final String mapClientId;

	private final String mapClientSecret;

	private final String searchClientId;

	private final String searchClientSecret;

	public NaverLocationProperties(@Value("${NAVER_MAP_CLIENT_ID:}") String mapClientId,
			@Value("${NAVER_MAP_CLIENT_SECRET:}") String mapClientSecret,
			@Value("${NAVER_SEARCH_CLIENT_ID:}") String searchClientId,
			@Value("${NAVER_SEARCH_CLIENT_SECRET:}") String searchClientSecret) {
		this.mapClientId = mapClientId;
		this.mapClientSecret = mapClientSecret;
		this.searchClientId = searchClientId;
		this.searchClientSecret = searchClientSecret;
	}

	public String getMapClientId() {
		return mapClientId;
	}

	public String getMapClientSecret() {
		return mapClientSecret;
	}

	public String getSearchClientId() {
		return searchClientId;
	}

	public String getSearchClientSecret() {
		return searchClientSecret;
	}

	public boolean isConfigured() {
		return isSearchConfigured() || isMapConfigured();
	}

	public boolean isMapConfigured() {
		return !mapClientId.isBlank() && !mapClientSecret.isBlank();
	}

	public boolean isSearchConfigured() {
		return !searchClientId.isBlank() && !searchClientSecret.isBlank();
	}

}
