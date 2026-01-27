package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.ApiControllerAdvice;
import com.knock.core.domain.notification.NotificationService;
import com.knock.core.domain.notification.dto.NotificationResult;
import com.knock.core.enums.NotificationType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.notification.Notification;
import com.knock.test.api.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.createMember;
import static com.knock.test.api.RestDocsUtils.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class NotificationControllerTest extends RestDocsTest {

	private NotificationService notificationService;

	private NotificationController notificationController;

	private MemberPrincipal principal;

	@BeforeEach
	public void setUp() {
		notificationService = mock(NotificationService.class);
		notificationController = new NotificationController(notificationService);
		principal = new MemberPrincipal(1L, TEST_EMAIL, "ROLE_USER");
		mockMvc = mockController(notificationController, new ApiControllerAdvice(), principalResolver(principal));
	}

	@Test
	@DisplayName("내 알림 목록 조회 성공")
	void getMyNotifications_success() {
		// given
		Member member = createMember(TEST_MEMBER_ID);
		Notification notification = Notification.create(member, NotificationType.RESERVATION_CREATED, "테스트 알림",
				"/test");
		ReflectionTestUtils.setField(notification, "id", TEST_NOTIFICATION_ID);
		NotificationResult result = NotificationResult.from(notification);

		given(notificationService.getMyNotifications(anyLong())).willReturn(List.of(result));

		// when & then
		restDocGiven().get("/api/v1/notifications")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/notifications/my-list", requestPreprocessor(), responsePreprocessor(),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("알림 ID"),
							fieldWithPath("data[].notificationType").type(JsonFieldType.STRING).description("알림 유형"),
							fieldWithPath("data[].content").type(JsonFieldType.STRING).description("알림 내용"),
							fieldWithPath("data[].relatedUrl").type(JsonFieldType.STRING).description("이동 URL"),
							fieldWithPath("data[].isRead").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
							fieldWithPath("data[].createdAt").description("생성일"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

	@Test
	@DisplayName("알림 읽음 처리 성공")
	void markAsRead_success() {
		// when & then
		restDocGiven().pathParam("id", TEST_NOTIFICATION_ID)
			.patch("/api/v1/notifications/{id}/read")
			.then()
			.status(HttpStatus.OK)
			.apply(document("api/v1/notifications/read", requestPreprocessor(), responsePreprocessor(),
					pathParameters(parameterWithName("id").description("알림 ID")),
					relaxedResponseFields(fieldWithPath("result").type(JsonFieldType.STRING).description("결과 코드"),
							fieldWithPath("data").type(JsonFieldType.NULL).description("데이터"),
							fieldWithPath("error").type(JsonFieldType.NULL).description("에러 정보"))));
	}

}
