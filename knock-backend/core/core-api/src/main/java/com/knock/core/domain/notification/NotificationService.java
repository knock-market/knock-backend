package com.knock.core.domain.notification;

import com.knock.core.domain.notification.dto.NotificationCreateData;
import com.knock.core.domain.notification.dto.NotificationResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.notification.Notification;
import com.knock.storage.db.core.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	private final MemberRepository memberRepository;

	@Transactional
	public Long createNotification(NotificationCreateData data) {
		Member member = memberRepository.findById(data.memberId())
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		Notification notification = Notification.create(member, data.notificationType(), data.content(),
				data.relatedUrl());

		Notification saved = notificationRepository.save(notification);
		return saved.getId();
	}

	public List<NotificationResult> getMyNotifications(Long memberId) {
		List<Notification> notifications = notificationRepository.findByMemberId(memberId);
		return notifications.stream().map(NotificationResult::from).collect(Collectors.toList());
	}

	@Async
	@Transactional
	public void markAsRead(Long memberId, Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new CoreException(ErrorType.NOTIFICATION_NOT_FOUND));

		if (notification.isOwner(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		notification.markAsRead();
	}

}
