package com.knock.core.domain.group;

import com.knock.core.api.controller.v1.response.InviteCodeResponseDto;
import com.knock.core.domain.group.dto.GroupData;
import com.knock.core.enums.InviteDuration;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupMember;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("그룹 생성 성공")
    void createGroup_success() {
        // given
        Long memberId = 1L;
        GroupData.Create request = new GroupData.Create("My Group", "Description");
        Member member = Member.builder().name("User").email("test@test.com").password("pw").nickname("nick")
                .provider("local").build();
        ReflectionTestUtils.setField(member, "id", memberId);

        Group group = Group.create(request.name(), request.description(), "INVITE", memberId);
        ReflectionTestUtils.setField(group, "id", 10L);

        given(groupRepository.save(any(Group.class))).willReturn(group);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        Long groupId = groupService.createGroup(memberId, request);

        // then
        assertThat(groupId).isEqualTo(10L);
        verify(groupRepository).save(any(Group.class));
        verify(groupRepository).saveMember(any(Group.class), any(Member.class), any(GroupMember.GroupRole.class));
    }

    @Test
    @DisplayName("개인 그룹 생성 성공")
    void createPersonalGroup_success() {
        // given
        Long memberId = 1L;
        String memberName = "User";
        Member member = Member.builder().name(memberName).email("test@test.com").password("pw").build();
        ReflectionTestUtils.setField(member, "id", memberId);

        Group group = Group.createPersonal(memberName + "의 당근", "desc", "INVITE", memberId);
        given(groupRepository.save(any(Group.class))).willReturn(group);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        groupService.createPersonalGroup(memberId, memberName);

        // then
        verify(groupRepository).save(any(Group.class));
        verify(groupRepository).saveMember(any(Group.class), any(Member.class), any(GroupMember.GroupRole.class));
    }

    @Test
    @DisplayName("초대 코드 생성 - 시간 제한")
    void generateTimedInviteCode_success() {
        // given
        Long memberId = 1L;
        Long groupId = 10L;
        Member member = Member.builder().name("User").build();
        ReflectionTestUtils.setField(member, "id", memberId);

        Group group = Group.create("Group", "Desc", "OLDCODE", memberId);
        ReflectionTestUtils.setField(group, "id", groupId);

        given(groupRepository.findGroupByGroupId(groupId)).willReturn(Optional.of(group));

        // when
        InviteCodeResponseDto response = groupService.generateTimedInviteCode(memberId, groupId,
                InviteDuration.ONE_HOUR);

        // then
        assertThat(response.inviteCode()).isNotEqualTo("OLDCODE");
        assertThat(response.expiresAt()).isAfter(LocalDateTime.now());
        assertThat(group.getInviteCode()).isEqualTo(response.inviteCode());
    }

    @Test
    @DisplayName("그룹 가입 실패 - 초대 코드 만료")
    void joinGroup_fail_expired() {
        // given
        Long memberId = 2L;
        String inviteCode = "EXPIRED";
        GroupData.Join request = new GroupData.Join(inviteCode);

        Member member = Member.builder().name("User2").build();
        Group group = Group.create("Group", "Desc", inviteCode, 1L);
        // 만료 시간 설정 (1시간 전)
        group.updateInviteCode(inviteCode, LocalDateTime.now().minusHours(1));

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(groupRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupService.joinGroup(memberId, request))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.INVITE_CODE_EXPIRED);
    }
}
