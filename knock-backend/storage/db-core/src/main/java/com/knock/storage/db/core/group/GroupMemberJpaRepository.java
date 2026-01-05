package com.knock.storage.db.core.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMember, Long> {

	boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);

	Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

	List<GroupMember> findByMemberId(Long memberId);

}
