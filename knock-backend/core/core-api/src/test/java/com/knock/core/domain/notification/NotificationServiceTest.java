package com.knock.core.domain.notification;

import com.knock.core.domain.notification.dto.NotificationCreateData;
import com.knock.core.domain.notification.dto.NotificationResult;
import com.knock.core.enums.NotificationType;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.notification.Notification;
import com.knock.storage.db.core.notification.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private MemberRepository memberRepository;

	@Nested
	@DisplayName("알림 생성")
	class CreateNotification {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			NotificationCreateData data = new NotificationCreateData(TEST_MEMBER_ID,
					NotificationType.RESERVATION_CREATED, "예약이 들어왔습니다.", "/items/1");
			Notification notification = Notification.create(member, data.notificationType(), data.content(),
					data.relatedUrl());
			ReflectionTestUtils.setField(notification, "id", TEST_NOTIFICATION_ID);

			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(notificationRepository.save(any(Notification.class))).willReturn(notification);

			// when
			Long result = notificationService.createNotification(data);

			// then
			assertThat(result).isEqualTo(TEST_NOTIFICATION_ID);
			verify(notificationRepository).save(any(Notification.class));
		}

		@Test
		@DisplayName("실패 - 회원 없음")
		void fail_memberNotFound() {
			// given
			NotificationCreateData data = new NotificationCreateData(TEST_MEMBER_ID,
					NotificationType.RESERVATION_CREATED, "예약이 들어왔습니다.", "/items/1");
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> notificationService.createNotification(data)).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
		}

	}

	@Nested
	@DisplayName("내 알림 조회")
	class GetMyNotifications {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Notification notification = Notification.create(member, NotificationType.RESERVATION_CREATED, "테스트",
					"/test");
			ReflectionTestUtils.setField(notification, "id", TEST_NOTIFICATION_ID);

			given(notificationRepository.findByMemberId(TEST_MEMBER_ID)).willReturn(List.of(notification));

			// when
			List<NotificationResult> results = notificationService.getMyNotifications(TEST_MEMBER_ID);

			// then
			assertThat(results).hasSize(1);
		}

		@Test
		@DisplayName("빈 목록")
		void emptyList() {
			// given
			given(notificationRepository.findByMemberId(TEST_MEMBER_ID)).willReturn(List.of());

			// when
			List<NotificationResult> results = notificationService.getMyNotifications(TEST_MEMBER_ID);

			// then
			assertThat(results).isEmpty();
		}

	}

	@Nested
	@DisplayName("알림 읽음 처리")
	class MarkAsRead {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Notification notification = Notification.create(member, NotificationType.RESERVATION_CREATED, "테스트",
					"/test");
			ReflectionTestUtils.setField(notification, "id", TEST_NOTIFICATION_ID);

			given(notificationRepository.findById(TEST_NOTIFICATION_ID)).willReturn(Optional.of(notification));

			// when
			notificationService.markAsRead(TEST_MEMBER_ID, TEST_NOTIFICATION_ID);

			// then
			assertThat(notification.isRead()).isTrue();
		}

		@Test
		@DisplayName("실패 - 알림 없음")
		void fail_notificationNotFound() {
			// given
			given(notificationRepository.findById(TEST_NOTIFICATION_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> notificationService.markAsRead(TEST_MEMBER_ID, TEST_NOTIFICATION_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.NOTIFICATION_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 권한 없음")
		void fail_forbidden() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Notification notification = Notification.create(member, NotificationType.RESERVATION_CREATED, "테스트",
					"/test");
			ReflectionTestUtils.setField(notification, "id", TEST_NOTIFICATION_ID);

			given(notificationRepository.findById(TEST_NOTIFICATION_ID)).willReturn(Optional.of(notification));

			// when & then - Different member (ID=2) tries to mark as read
			assertThatThrownBy(() -> notificationService.markAsRead(TEST_MEMBER_ID_2, TEST_NOTIFICATION_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		}

	}

}
