package com.knock.storage.db.core.notification;

import com.knock.core.enums.NotificationType;
import com.knock.storage.db.core.config.QueryDslConfig;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class NotificationRepositoryTest {

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
}
