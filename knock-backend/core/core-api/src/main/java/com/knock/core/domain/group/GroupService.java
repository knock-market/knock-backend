package com.knock.core.domain.group;

import com.knock.core.domain.group.dto.GroupCreateData;
import com.knock.core.domain.group.dto.GroupJoinData;
import com.knock.core.domain.group.dto.GroupInviteCodeResult;
import com.knock.core.domain.group.dto.GroupResult;
import com.knock.core.enums.InviteDuration;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupMember;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public Long createGroup(Long memberId, GroupCreateData data) {
		String inviteCode = generateUniqueInviteCode();
		Group group = Group.create(data.name(), data.description(), inviteCode, memberId);
		Group savedGroup = groupRepository.save(group);

		Member ownerMember = memberRepository.findById(memberId)
				.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		groupRepository.saveMember(savedGroup, ownerMember, GroupMember.GroupRole.ADMIN);

		return savedGroup.getId();
	}

	@Transactional
	public void createPersonalGroup(Long memberId, String memberName) {
		String inviteCode = generateUniqueInviteCode();
		String groupName = memberName + "의 노크마켓";
		String description = memberName + "님의 개인 거래 공간입니다.";

		Group group = Group.createPersonal(groupName, description, inviteCode, memberId);
		Group savedGroup = groupRepository.save(group);

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		groupRepository.saveMember(savedGroup, member, GroupMember.GroupRole.ADMIN);
	}

	@Transactional
	public GroupInviteCodeResult generateTimedInviteCode(Long memberId, Long groupId, InviteDuration duration) {
		Group group = groupRepository.findGroupByGroupId(groupId)
				.orElseThrow(() -> new CoreException(ErrorType.GROUP_NOT_FOUND));

		if (!group.getOwnerId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		String newInviteCode = generateUniqueInviteCode();
		LocalDateTime expiresAt = duration.getDuration() != null ? LocalDateTime.now().plus(duration.getDuration())
				: null;

		group.updateInviteCode(newInviteCode, expiresAt);
		return new GroupInviteCodeResult(newInviteCode, expiresAt);
	}

	@Transactional
	public Long joinGroup(Long memberId, GroupJoinData data) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		Group group = groupRepository.findByInviteCode(data.inviteCode())
				.orElseThrow(() -> new CoreException(ErrorType.INVALID_INVITE_CODE));

		if (group.isInviteCodeExpired()) {
			throw new CoreException(ErrorType.INVITE_CODE_EXPIRED);
		}

		if (groupRepository.existsMember(group.getId(), member.getId())) {
			throw new CoreException(ErrorType.ALREADY_MEMBER);
		}

		groupRepository.saveMember(group, member, GroupMember.GroupRole.MEMBER);

		return group.getId();
	}

	@Transactional
	public void leaveGroup(Long memberId, Long groupId) {
		GroupMember groupMember = groupRepository.findMember(groupId, memberId)
				.orElseThrow(() -> new CoreException(ErrorType.GROUP_NOT_FOUND));
		groupRepository.deleteMember(groupMember);
	}

	@Transactional(readOnly = true)
	public List<GroupResult> getMyGroups(Long memberId) {
		return groupRepository.findGroupMembersByMemberId(memberId)
				.stream()
				.map(GroupMember::getGroup)
				.map(GroupResult::from)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public GroupResult getGroupDetail(Long groupId) {
		Group group = groupRepository.findGroupByGroupId(groupId)
				.orElseThrow(() -> new CoreException(ErrorType.GROUP_NOT_FOUND));
		return GroupResult.from(group);
	}

	private String generateUniqueInviteCode() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

}
