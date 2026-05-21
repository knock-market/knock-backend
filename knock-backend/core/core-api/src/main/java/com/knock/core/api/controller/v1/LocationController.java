package com.knock.core.api.controller.v1;

import com.knock.core.api.controller.v1.response.LocationSearchResponseDto;
import com.knock.core.domain.location.LocationSearchService;
import com.knock.core.domain.location.dto.LocationSearchResult;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocationController {

	private final LocationSearchService locationSearchService;

	@GetMapping("/api/v1/locations/search")
	public ApiResponse<List<LocationSearchResponseDto>> searchLocations(@RequestParam String query) {
		List<LocationSearchResult> results = locationSearchService.search(query);
		return ApiResponse.success(results.stream().map(LocationSearchResponseDto::from).toList());
	}

}
