package com.knock.storage.db.core.group;

import com.knock.storage.db.core.member.Member;
import java.util.List;
import java.util.Optional;

public interface GroupRepository {

	Group save(Group group);

	Optional<Group> findByInviteCode(String inviteCode);

	GroupMember saveMember(Group group, Member member, GroupMember.GroupRole role);

	boolean existsMember(Long groupId, Long memberId);

	Optional<GroupMember> findMember(Long groupId, Long memberId);

	void deleteMember(GroupMember groupMember);

	List<GroupMember> findGroupMembersByMemberId(Long memberId);

	Optional<Group> findGroupByGroupId(Long groupId);

}
