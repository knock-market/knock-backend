package com.knock.storage.db.core.notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

	Notification save(Notification notification);

	Optional<Notification> findById(Long id);

	List<Notification> findByMemberId(Long memberId);

	void delete(Notification notification);

}
