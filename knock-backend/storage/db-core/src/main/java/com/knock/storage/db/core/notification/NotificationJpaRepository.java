package com.knock.storage.db.core.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByMember_IdOrderByCreatedAtDesc(Long memberId);

}
