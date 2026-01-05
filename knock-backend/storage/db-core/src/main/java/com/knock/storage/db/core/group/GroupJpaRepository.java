package com.knock.storage.db.core.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupJpaRepository extends JpaRepository<Group, Long> {

	Optional<Group> findByInviteCode(String inviteCode);

}
