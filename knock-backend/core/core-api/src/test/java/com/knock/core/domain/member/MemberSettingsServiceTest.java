package com.knock.core.domain.member;

import com.knock.core.domain.member.dto.MemberNotificationSettingsResult;
import com.knock.core.domain.member.dto.MemberNotificationSettingsUpdateData;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.knock.core.support.TestConstants.TEST_MEMBER_ID;
import static com.knock.core.support.TestFixtures.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberSettingsServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Nested
	@DisplayName("알림 설정")
	class NotificationSettings {

		@Test
		@DisplayName("조회 성공")
		void getSettings_success() {
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));

			MemberNotificationSettingsResult result = memberService.getNotificationSettings(TEST_MEMBER_ID);

			assertThat(result.push()).isTrue();
			assertThat(result.newItems()).isTrue();
			assertThat(result.chat()).isTrue();
			assertThat(result.marketing()).isFalse();
			assertThat(result.sound()).isTrue();
		}

		@Test
		@DisplayName("수정 성공")
		void updateSettings_success() {
			Member member = createMember(TEST_MEMBER_ID);
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			MemberNotificationSettingsUpdateData data = new MemberNotificationSettingsUpdateData(false, true, false,
					true, false);

			memberService.updateNotificationSettings(TEST_MEMBER_ID, data);

			assertThat(member.isNotificationPushEnabled()).isFalse();
			assertThat(member.isNotificationNewItemsEnabled()).isTrue();
			assertThat(member.isNotificationChatEnabled()).isFalse();
			assertThat(member.isNotificationMarketingEnabled()).isTrue();
			assertThat(member.isNotificationSoundEnabled()).isFalse();
		}

		@Test
		@DisplayName("실패 - 회원 없음")
		void fail_memberNotFound() {
			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());
			MemberNotificationSettingsUpdateData data = new MemberNotificationSettingsUpdateData(false, false, false,
					false, false);

			assertThatThrownBy(() -> memberService.updateNotificationSettings(TEST_MEMBER_ID, data))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
		}

	}

}
