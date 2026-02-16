package com.knock.storage.db.core.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByMember_IdOrderByCreatedAtDesc(Long memberId);

	@Modifying(clearAutomatically = true)
	@Query("""
			UPDATE Notification n
			SET n.isRead = true
			WHERE n.member.id = :memberId
			AND n.isRead = false
			AND n.deletedAt IS NULL
			""")
	int markAllAsReadByMemberId(@Param("memberId") Long memberId);

}
