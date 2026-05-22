package com.knock.core.domain.location.dto;

public record LocationSearchResult(String name, String address, Double latitude, Double longitude, Double naverMapX,
		Double naverMapY) {
}
