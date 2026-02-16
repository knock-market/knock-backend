package com.knock.storage.db.core.notification;

import com.knock.core.enums.NotificationType;
import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRepositoryTest extends CoreDbContextTest {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("멤버별 알림 목록 조회")
	void findAllByMemberIdOrderByCreatedAtDesc() {
		// given
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		notificationRepository
			.save(Notification.create(member, NotificationType.RESERVATION_CREATED, "Content 1", "/path1"));
		notificationRepository
			.save(Notification.create(member, NotificationType.RESERVATION_CREATED, "Content 2", "/path2"));

		// when
		List<Notification> notifications = notificationRepository.findByMemberId(member.getId());

		// then
		assertThat(notifications).hasSize(2);
	}

	@Test
	@DisplayName("멤버별 알림 전체 읽음 처리")
	void markAllAsReadByMemberId() {
		// given
		Member member1 = memberRepository.save(Member.create("member1@test.com", "Name1", "Pass", "Nick1", "LOCAL"));
		Member member2 = memberRepository.save(Member.create("member2@test.com", "Name2", "Pass", "Nick2", "LOCAL"));
		notificationRepository.save(Notification.create(member1, NotificationType.RESERVATION_CREATED, "Content 1", "/path1"));
		notificationRepository.save(Notification.create(member1, NotificationType.RESERVATION_CREATED, "Content 2", "/path2"));
		notificationRepository.save(Notification.create(member2, NotificationType.RESERVATION_CREATED, "Content 3", "/path3"));

		// when
		int updatedCount = notificationRepository.markAllAsReadByMemberId(member1.getId());
		List<Notification> member1Notifications = notificationRepository.findByMemberId(member1.getId());
		List<Notification> member2Notifications = notificationRepository.findByMemberId(member2.getId());

		// then
		assertThat(updatedCount).isEqualTo(2);
		assertThat(member1Notifications).allMatch(Notification::isRead);
		assertThat(member2Notifications).noneMatch(Notification::isRead);
	}

}
