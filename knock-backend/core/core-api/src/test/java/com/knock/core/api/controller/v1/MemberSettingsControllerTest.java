package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.api.controller.v1.request.NotificationSettingsUpdateRequestDto;
import com.knock.core.domain.member.MemberService;
import com.knock.core.domain.member.dto.MemberNotificationSettingsResult;
import com.knock.test.api.RestDocsTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.knock.core.support.TestConstants.TEST_EMAIL;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;

class MemberSettingsControllerTest extends RestDocsTest {

	private MemberService memberService;

	private MemberSettingsController memberSettingsController;

	private MemberPrincipal principal;

	@BeforeEach
	void setUp() {
		memberService = mock(MemberService.class);
		memberSettingsController = new MemberSettingsController(memberService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(memberSettingsController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("알림 설정 조회 성공")
	void getNotificationSettings_success() {
		// given
		MemberNotificationSettingsResult result = new MemberNotificationSettingsResult(true, true, true, false, true);
		given(memberService.getNotificationSettings(any())).willReturn(result);

		// when & then
		restDocGiven().get("/api/v1/members/my/settings/notifications")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/members/settings/notifications/get", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data.push").type(JsonFieldType.BOOLEAN).description("전체 푸시 알림"),
							fieldWithPath("data.newItems").type(JsonFieldType.BOOLEAN).description("새 상품 알림"),
							fieldWithPath("data.chat").type(JsonFieldType.BOOLEAN).description("채팅 알림"),
							fieldWithPath("data.marketing").type(JsonFieldType.BOOLEAN).description("마케팅 알림"),
							fieldWithPath("data.sound").type(JsonFieldType.BOOLEAN).description("알림 소리"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("알림 설정 수정 성공")
	void updateNotificationSettings_success() {
		// given
		NotificationSettingsUpdateRequestDto request = new NotificationSettingsUpdateRequestDto(true, true, false,
				false, true);
		doNothing().when(memberService).updateNotificationSettings(any(), any());

		// when & then
		restDocGiven().contentType(ContentType.JSON)
			.body(request)
			.put("/api/v1/members/my/settings/notifications")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/members/settings/notifications/put", requestPreprocessor(), responsePreprocessor(),
					relaxedRequestFields(fieldWithPath("push").type(JsonFieldType.BOOLEAN).description("전체 푸시 알림"),
							fieldWithPath("newItems").type(JsonFieldType.BOOLEAN).description("새 상품 알림"),
							fieldWithPath("chat").type(JsonFieldType.BOOLEAN).description("채팅 알림"),
							fieldWithPath("marketing").type(JsonFieldType.BOOLEAN).description("마케팅 알림"),
							fieldWithPath("sound").type(JsonFieldType.BOOLEAN).description("알림 소리")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
