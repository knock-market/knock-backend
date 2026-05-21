package com.knock.core.api.controller.v1;

import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.location.LocationSearchService;
import com.knock.core.domain.location.dto.LocationSearchResult;
import com.knock.test.api.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

class LocationControllerTest extends RestDocsTest {

	private LocationSearchService locationSearchService;

	private LocationController locationController;

	@BeforeEach
	public void setUp() {
		locationSearchService = mock(LocationSearchService.class);
		locationController = new LocationController(locationSearchService);
		mockMvc = mockController(locationController, new ApiControllerAdvice());
	}

	@Test
	@DisplayName("위치 검색 성공")
	void searchLocations_success() {
		// given
		given(locationSearchService.search("성수역")).willReturn(
				List.of(new LocationSearchResult("성수역", "서울 성동구 아차산로 100", 37.544, 127.056, 321234.0, 543210.0)));

		// when & then
		restDocGiven().queryParam("query", "성수역")
			.get("/api/v1/locations/search")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/locations/search", requestPreprocessor(), responsePreprocessor(),
					queryParameters(parameterWithName("query").description("검색어. 장소명, 역명, 도로명/지번 주소")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].name").type(JsonFieldType.STRING).description("장소 이름"),
							fieldWithPath("data[].address").type(JsonFieldType.STRING).description("주소"),
							fieldWithPath("data[].latitude").type(JsonFieldType.NUMBER)
								.description("위도. Geocoding 보강 실패 시 null")
								.optional(),
							fieldWithPath("data[].longitude").type(JsonFieldType.NUMBER)
								.description("경도. Geocoding 보강 실패 시 null")
								.optional(),
							fieldWithPath("data[].naverMapX").type(JsonFieldType.NUMBER)
								.description("Naver Local Search 지도 x 좌표")
								.optional(),
							fieldWithPath("data[].naverMapY").type(JsonFieldType.NUMBER)
								.description("Naver Local Search 지도 y 좌표")
								.optional(),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
