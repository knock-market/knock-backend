package com.knock.storage.db.core.group;

import com.knock.storage.db.core.member.Member;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

	private final GroupJpaRepository groupJpaRepository;

	private final GroupMemberJpaRepository groupMemberJpaRepository;

	public GroupRepositoryImpl(GroupJpaRepository groupJpaRepository, GroupMemberJpaRepository groupMemberJpaRepository) {
		this.groupJpaRepository = groupJpaRepository;
		this.groupMemberJpaRepository = groupMemberJpaRepository;
	}

	@Override
	public Group save(Group group) {
		return groupJpaRepository.save(group);
	}

	@Override
	public Optional<Group> findByInviteCode(String inviteCode) {
		return groupJpaRepository.findByInviteCode(inviteCode);
	}

	@Override
	public GroupMember saveMember(Group group, Member member, GroupMember.GroupRole role) {
		GroupMember groupMember = GroupMember.create(group, member, role);
		return groupMemberJpaRepository.save(groupMember);
	}

	@Override
	public boolean existsMember(Long groupId, Long memberId) {
		return groupMemberJpaRepository.existsByGroupIdAndMemberId(groupId, memberId);
	}

	@Override
	public Optional<GroupMember> findMember(Long groupId, Long memberId) {
		return groupMemberJpaRepository.findByGroupIdAndMemberId(groupId, memberId);
	}

	@Override
	public void deleteMember(GroupMember groupMember) {
		groupMemberJpaRepository.delete(groupMember);
	}

	@Override
	public List<GroupMember> findGroupMembersByMemberId(Long memberId) {
		return groupMemberJpaRepository.findByMemberId(memberId);
	}

	@Override
	public Optional<Group> findGroupByGroupId(Long groupId) {
		return groupJpaRepository.findById(groupId);
	}

}