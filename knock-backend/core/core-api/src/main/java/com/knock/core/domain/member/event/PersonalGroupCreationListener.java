package com.knock.core.domain.member.event;

import com.knock.core.domain.group.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalGroupCreationListener {

	private final GroupService groupService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMemberCreatedEvent(MemberCreatedEvent event) {
		log.info("Creating personal group for member: {}", event.memberId());
		try {
			groupService.createPersonalGroup(event.memberId(), event.memberName());
		}
		catch (Exception e) {
			log.error("Failed to create personal group for member: {}", event.memberId(), e);
		}
	}

}
