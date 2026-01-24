package com.knock.core.domain.member;

import com.knock.core.domain.member.dto.MemberResult;
import com.knock.core.domain.member.dto.MemberSignupData;
import com.knock.core.domain.member.dto.MemberSignupResult;
import com.knock.core.domain.member.event.MemberCreatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            MemberSignupData data = new MemberSignupData(
                    TEST_EMAIL, TEST_PASSWORD, TEST_NAME, TEST_NICKNAME, null, TEST_PROVIDER);
            Member savedMember = createMember(TEST_MEMBER_ID);

            given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            // when
            MemberSignupResult result = memberService.signup(data);

            // then
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
            verify(memberRepository).save(any(Member.class));
            verify(eventPublisher).publishEvent(any(MemberCreatedEvent.class));
        }

        @Test
        @DisplayName("실패 - 이메일 중복")
        void fail_duplicateEmail() {
            // given
            MemberSignupData data = new MemberSignupData(
                    TEST_EMAIL, TEST_PASSWORD, TEST_NAME, TEST_NICKNAME, null, TEST_PROVIDER);

            given(memberRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.signup(data))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_EMAIL);
        }
    }

    @Nested
    @DisplayName("회원 조회")
    class GetMember {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));

            // when
            MemberResult result = memberService.getMember(TEST_MEMBER_ID);

            // then
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("실패 - 회원 없음")
        void fail_memberNotFound() {
            // given
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(TEST_MEMBER_ID))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
        }
    }
}
