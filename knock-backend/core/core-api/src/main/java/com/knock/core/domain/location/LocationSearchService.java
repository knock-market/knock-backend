package com.knock.core.domain.location;

import com.knock.core.domain.location.NaverLocationClient.NaverAddress;
import com.knock.core.domain.location.NaverLocationClient.NaverGeocodeResponse;
import com.knock.core.domain.location.NaverLocationClient.NaverLocalPlace;
import com.knock.core.domain.location.NaverLocationClient.NaverLocalSearchResponse;
import com.knock.core.domain.location.dto.LocationSearchResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LocationSearchService {

	private static final int SEARCH_LIMIT = 10;

	private static final int MAX_QUERY_LENGTH = 100;

	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

	private static final double NAVER_LOCAL_COORDINATE_SCALE = 10_000_000.0;

	private final NaverLocationClient naverLocationClient;

	private final NaverLocationProperties properties;

	public List<LocationSearchResult> search(String query) {
		String normalizedQuery = normalizeQuery(query);
		if (!properties.isConfigured()) {
			throw new CoreException(ErrorType.LOCATION_SEARCH_UNAVAILABLE);
		}
		if (!properties.isSearchConfigured() && !looksLikeAddress(normalizedQuery)) {
			throw new CoreException(ErrorType.LOCATION_SEARCH_UNAVAILABLE);
		}

		try {
			List<LocationSearchResult> placeResults = searchPlaces(normalizedQuery);
			if (!placeResults.isEmpty()) {
				return placeResults;
			}
			return geocodeAddress(normalizedQuery).map(List::of).orElseGet(List::of);
		}
		catch (RestClientException e) {
			return List.of();
		}
	}

	private List<LocationSearchResult> searchPlaces(String query) {
		if (!properties.isSearchConfigured()) {
			return List.of();
		}

		NaverLocalSearchResponse response;
		try {
			response = naverLocationClient.searchLocal(query, SEARCH_LIMIT, properties);
		}
		catch (RestClientException e) {
			return List.of();
		}

		if (response == null || response.items() == null) {
			return List.of();
		}

		return response.items()
			.stream()
			.map(place -> toSearchResult(place).orElse(null))
			.filter(result -> result != null)
			.limit(SEARCH_LIMIT)
			.toList();
	}

	private Optional<LocationSearchResult> toSearchResult(NaverLocalPlace place) {
		String address = firstPresent(place.roadAddress(), place.address());
		if (address == null) {
			return Optional.empty();
		}

		LocationSearchResult baseResult = new LocationSearchResult(cleanTitle(place.title()), address,
				parseLocalLatitude(place.mapy()), parseLocalLongitude(place.mapx()), parseCoordinate(place.mapx()),
				parseCoordinate(place.mapy()));
		try {
			return Optional.of(geocodeAddress(address)
				.map(result -> new LocationSearchResult(baseResult.name(), result.address(), result.latitude(),
						result.longitude(), baseResult.naverMapX(), baseResult.naverMapY()))
				.orElse(baseResult));
		}
		catch (RestClientException e) {
			return Optional.of(baseResult);
		}
	}

	private Optional<LocationSearchResult> geocodeAddress(String query) {
		if (!properties.isMapConfigured()) {
			return Optional.empty();
		}

		NaverGeocodeResponse response = naverLocationClient.geocode(query, properties);
		if (response == null || response.addresses() == null || response.addresses().isEmpty()) {
			return Optional.empty();
		}

		NaverAddress address = response.addresses().getFirst();
		Double latitude = parseCoordinate(address.y());
		Double longitude = parseCoordinate(address.x());
		if (latitude == null || longitude == null) {
			return Optional.empty();
		}

		String label = firstPresent(address.roadAddress(), address.jibunAddress(), query);
		return Optional.of(new LocationSearchResult(label, label, latitude, longitude, null, null));
	}

	private String normalizeQuery(String query) {
		if (query == null || query.isBlank() || query.trim().length() > MAX_QUERY_LENGTH) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
		return query.trim();
	}

	private String cleanTitle(String value) {
		if (value == null || value.isBlank()) {
			return "Pickup location";
		}
		return HTML_TAG_PATTERN.matcher(value).replaceAll("").trim();
	}

	private boolean looksLikeAddress(String query) {
		return query.matches(".*(시|군|구|동|로|길|번지|대로|읍|면|리|\\d).*");
	}

	private Double parseCoordinate(String value) {
		try {
			return value == null ? null : Double.valueOf(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private Double parseLocalLatitude(String value) {
		Double coordinate = parseCoordinate(value);
		if (coordinate == null) {
			return null;
		}
		double latitude = coordinate / NAVER_LOCAL_COORDINATE_SCALE;
		return isLatitude(latitude) ? latitude : null;
	}

	private Double parseLocalLongitude(String value) {
		Double coordinate = parseCoordinate(value);
		if (coordinate == null) {
			return null;
		}
		double longitude = coordinate / NAVER_LOCAL_COORDINATE_SCALE;
		return isLongitude(longitude) ? longitude : null;
	}

	private boolean isLatitude(double value) {
		return value >= -90 && value <= 90;
	}

	private boolean isLongitude(double value) {
		return value >= -180 && value <= 180;
	}

	private String firstPresent(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value.trim();
			}
		}
		return null;
	}

}
