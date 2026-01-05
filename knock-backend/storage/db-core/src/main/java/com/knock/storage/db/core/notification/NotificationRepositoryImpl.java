package com.knock.storage.db.core.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

	private final NotificationJpaRepository jpaRepository;

	@Override
	public Notification save(Notification notification) {
		return jpaRepository.save(notification);
	}

	@Override
	public Optional<Notification> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<Notification> findByMemberId(Long memberId) {
		return jpaRepository.findByMember_IdOrderByCreatedAtDesc(memberId);
	}

	@Override
	public void delete(Notification notification) {
		jpaRepository.delete(notification);
	}

}
