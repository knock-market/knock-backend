package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.location.dto.LocationSearchResult;

public record LocationSearchResponseDto(String name, String address, Double latitude, Double longitude,
		Double naverMapX, Double naverMapY) {

	public static LocationSearchResponseDto from(LocationSearchResult result) {
		return new LocationSearchResponseDto(result.name(), result.address(), result.latitude(), result.longitude(),
				result.naverMapX(), result.naverMapY());
	}

}
