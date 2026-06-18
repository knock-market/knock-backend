package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.ReportCreateRequestDto;
import com.knock.core.domain.report.ReportService;
import com.knock.core.domain.report.dto.ReportResult;
import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportStatus;
import com.knock.core.enums.ReportTargetType;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import java.time.LocalDateTime;

import static com.knock.core.support.TestConstants.*;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;

class ReportControllerTest extends RestDocsTest {

	private ReportService reportService;

	private ReportController reportController;

	private MemberPrincipal principal;

	@BeforeEach
	void setUp() {
		reportService = mock(ReportService.class);
		reportController = new ReportController(reportService);
		principal = new MemberPrincipal(TEST_MEMBER_ID, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(reportController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("신고 생성 성공")
	void createReport_success() {
		ReportCreateRequestDto request = new ReportCreateRequestDto(ReportTargetType.ITEM, TEST_ITEM_ID,
				ReportReason.PROHIBITED_ITEM, "금지 품목으로 의심됩니다.");
		given(reportService.createReport(anyLong(), any()))
			.willReturn(new ReportResult(10L, ReportStatus.RECEIVED, LocalDateTime.of(2026, 6, 18, 12, 0)));

		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/reports")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/reports/create", requestPreprocessor(), responsePreprocessor(),
					relaxedRequestFields(
							fieldWithPath("targetType").type(JsonFieldType.STRING)
								.description("신고 대상 타입: MEMBER, ITEM, RESERVATION, REVIEW"),
							fieldWithPath("targetId").type(JsonFieldType.NUMBER).description("신고 대상 ID"),
							fieldWithPath("reason").type(JsonFieldType.STRING)
								.description("신고 사유: PROHIBITED_ITEM, SUSPECTED_FRAUD, OFF_PLATFORM_PAYMENT, PERSONAL_INFO_OR_CODE_REQUEST, HARASSMENT_OR_THREAT, NO_SHOW, COUNTERFEIT_OR_STOLEN_SUSPECTED, OTHER"),
							fieldWithPath("description").type(JsonFieldType.STRING)
								.description("선택 설명. 최대 1000자.")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.reportId").type(JsonFieldType.NUMBER).description("신고 ID"),
							fieldWithPath("data.status").type(JsonFieldType.STRING).description("신고 상태"),
							fieldWithPath("data.createdAt").description("접수 시각"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("신고 생성 실패 - targetId 검증")
	void createReport_failValidation() {
		ReportCreateRequestDto request = new ReportCreateRequestDto(ReportTargetType.ITEM, 0L,
				ReportReason.PROHIBITED_ITEM, null);

		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.post("/api/v1/reports")
			.then()
			.status(HttpStatus.BAD_REQUEST);

		verifyNoInteractions(reportService);
	}

}
