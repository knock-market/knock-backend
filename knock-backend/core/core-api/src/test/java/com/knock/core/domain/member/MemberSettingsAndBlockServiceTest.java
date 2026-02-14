package com.knock.core.domain.member;

import com.knock.core.domain.member.dto.MemberNotificationSettingsResult;
import com.knock.core.domain.member.dto.MemberNotificationSettingsUpdateData;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberBlock;
import com.knock.storage.db.core.member.MemberBlockRepository;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberSettingsAndBlockServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private MemberBlockRepository memberBlockRepository;

	@Nested
	@DisplayName("알림 설정")
	class NotificationSettings {

		@Test
		@DisplayName("조회 성공")
		void getSettings_success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));

			// when
			MemberNotificationSettingsResult result = memberService.getNotificationSettings(TEST_MEMBER_ID);

			// then
			assertThat(result.push()).isTrue();
			assertThat(result.newItems()).isTrue();
			assertThat(result.chat()).isTrue();
			assertThat(result.marketing()).isFalse();
			assertThat(result.sound()).isTrue();
		}

		@Test
		@DisplayName("수정 성공")
		void updateSettings_success() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			MemberNotificationSettingsUpdateData data = new MemberNotificationSettingsUpdateData(false, true, false,
					true, false);

			// when
			memberService.updateNotificationSettings(TEST_MEMBER_ID, data);

			// then
			assertThat(member.isNotificationPushEnabled()).isFalse();
			assertThat(member.isNotificationNewItemsEnabled()).isTrue();
			assertThat(member.isNotificationChatEnabled()).isFalse();
			assertThat(member.isNotificationMarketingEnabled()).isTrue();
			assertThat(member.isNotificationSoundEnabled()).isFalse();
		}

		@Test
		@DisplayName("실패 - 회원 없음")
		void fail_memberNotFound() {
			// given
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());
			MemberNotificationSettingsUpdateData data = new MemberNotificationSettingsUpdateData(false, false, false,
					false, false);

			// when & then
			assertThatThrownBy(() -> memberService.updateNotificationSettings(TEST_MEMBER_ID, data))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
		}

	}

	@Nested
	@DisplayName("차단 유저")
	class BlockedMembers {

		@Test
		@DisplayName("차단 목록 조회 성공")
		void getBlockedMembers_success() {
			// given
			Member blocker = createMember(TEST_MEMBER_ID);
			Member blocked = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			MemberBlock memberBlock = MemberBlock.create(blocker, blocked);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(blocker));
			given(memberBlockRepository.findByBlockerId(TEST_MEMBER_ID)).willReturn(List.of(memberBlock));

			// when
			var result = memberService.getBlockedMembers(TEST_MEMBER_ID);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).id()).isEqualTo(TEST_MEMBER_ID_2);
		}

		@Test
		@DisplayName("차단 성공")
		void blockMember_success() {
			// given
			Member blocker = createMember(TEST_MEMBER_ID);
			Member blocked = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);

			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(blocker));
			given(memberRepository.findById(TEST_MEMBER_ID_2)).willReturn(Optional.of(blocked));
			given(memberBlockRepository.existsByBlockerIdAndBlockedId(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
				.willReturn(false);
			given(memberBlockRepository.save(any(MemberBlock.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			memberService.blockMember(TEST_MEMBER_ID, TEST_MEMBER_ID_2);

			// then
			verify(memberBlockRepository).save(any(MemberBlock.class));
		}

		@Test
		@DisplayName("실패 - 본인 차단")
		void blockMember_fail_selfBlock() {
			// when & then
			assertThatThrownBy(() -> memberService.blockMember(TEST_MEMBER_ID, TEST_MEMBER_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.VALIDATION_ERROR);
		}

		@Test
		@DisplayName("차단 해제 성공")
		void unblockMember_success() {
			// given
			Member blocker = createMember(TEST_MEMBER_ID);
			Member blocked = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			MemberBlock memberBlock = MemberBlock.create(blocker, blocked);

			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(blocker));
			given(memberRepository.findById(TEST_MEMBER_ID_2)).willReturn(Optional.of(blocked));
			given(memberBlockRepository.findByBlockerIdAndBlockedId(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
				.willReturn(Optional.of(memberBlock));

			// when
			memberService.unblockMember(TEST_MEMBER_ID, TEST_MEMBER_ID_2);

			// then
			verify(memberBlockRepository).delete(memberBlock);
		}

	}

}
