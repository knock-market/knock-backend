package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.ReservationCreateRequestDto;
import com.knock.core.domain.reservation.ReservationService;
import com.knock.core.domain.reservation.dto.ReservationResult;
import com.knock.core.enums.ReservationStatus;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class ReservationControllerTest extends RestDocsTest {

	private ReservationService reservationService;

	private ReservationController reservationController;

	private MemberPrincipal principal;

	@BeforeEach
	public void setUp() {
		reservationService = mock(ReservationService.class);
		reservationController = new ReservationController(reservationService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(reservationController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("예약 생성 성공")
	void createReservation_success() {
		// given
		ReservationCreateRequestDto request = new ReservationCreateRequestDto(TEST_ITEM_ID);
		given(reservationService.createReservation(any())).willReturn(TEST_RESERVATION_ID);

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/reservations")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/reservations/create", requestPreprocessor(), responsePreprocessor(),
					requestFields(fieldWithPath("itemId").type(JsonFieldType.NUMBER).description("상품 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NUMBER).description("생성된 예약 ID"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("예약 승인 성공")
	void approveReservation_success() {
		// when & then
		restDocGiven().pathParam("id", TEST_RESERVATION_ID)
			.patch("/api/v1/reservations/{id}/approve")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/reservations/approve", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("id").description("예약 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("내 예약 목록 조회 성공")
	void getMyReservations_success() {
		// given
		Member member = createMember(TEST_MEMBER_ID);
		Item item = createItem(TEST_ITEM_ID, createGroup(), member);
		Reservation reservation = createReservation(TEST_RESERVATION_ID, item, member, ReservationStatus.APPROVED);
		ReservationResult result = ReservationResult.from(reservation);

		given(reservationService.getMyReservations(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven().get("/api/v1/reservations/my")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/reservations/my-list", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("예약 ID"),
							fieldWithPath("data[].itemId").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data[].itemTitle").type(JsonFieldType.STRING).description("상품 제목"),
							fieldWithPath("data[].memberId").type(JsonFieldType.NUMBER).description("예약자 ID"),
							fieldWithPath("data[].memberName").type(JsonFieldType.STRING).description("예약자 이름"),
							fieldWithPath("data[].status").type(JsonFieldType.STRING).description("예약 상태"),
							fieldWithPath("data[].createdAt").description("예약 생성일"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
