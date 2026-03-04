package com.knock.auth;

import com.knock.auth.google.GoogleOAuthClient;
import com.knock.auth.google.GoogleOAuthClient.GoogleTokenResponse;
import com.knock.auth.google.GoogleOAuthClient.GoogleUserInfoResponse;
import com.knock.auth.google.GoogleOAuthProperties;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionAuthServiceGoogleTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private SecurityContextRepository securityContextRepository;

	@Mock
	private GoogleOAuthClient googleOAuthClient;

	private final GoogleOAuthProperties googleOAuthProperties = new GoogleOAuthProperties("google-client-id",
			"google-client-secret", "http://localhost:8080/api/v1/auth/social/google/callback");

	private SessionAuthService sessionAuthService;

	@BeforeEach
	void setUp() {
		sessionAuthService = new SessionAuthService(memberRepository, passwordEncoder, securityContextRepository,
				googleOAuthProperties, googleOAuthClient);
	}

	@Test
	@DisplayName("구글 인가 URL 생성 시 state를 세션에 저장한다")
	void getGoogleAuthorizationUrl_success() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		String authorizationUrl = sessionAuthService.getGoogleAuthorizationUrl("/item/100", request);

		String state = (String) request.getSession().getAttribute("GOOGLE_OAUTH_STATE");
		String next = (String) request.getSession().getAttribute("GOOGLE_OAUTH_NEXT");
		assertThat(state).isNotBlank();
		assertThat(next).isEqualTo("/item/100");
		assertThat(authorizationUrl).contains("client_id=google-client-id")
			.contains("redirect_uri=http://localhost:8080/api/v1/auth/social/google/callback")
			.contains("scope=openid")
			.contains("state=");
	}

	@Test
	@DisplayName("구글 OAuth 콜백 - state 불일치면 실패")
	void loginWithGoogle_fail_invalidState() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute("GOOGLE_OAUTH_STATE", "expected");
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertThatThrownBy(() -> sessionAuthService.loginWithGoogle("code", "different", request, response))
			.isInstanceOf(ResponseStatusException.class)
			.hasMessageContaining("OAuth state");
	}

	@Test
	@DisplayName("구글 OAuth 콜백 - 기존 소셜 계정 로그인")
	void loginWithGoogle_success_existingMember() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute("GOOGLE_OAUTH_STATE", "valid-state");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Member existingMember = createMember(1L, "google@test.com", "google", "google-sub");

		given(googleOAuthClient.getAccessToken(eq("auth-code"), any(GoogleOAuthProperties.class)))
			.willReturn(new GoogleTokenResponse("access-token"));
		given(googleOAuthClient.getUserInfo("access-token")).willReturn(new GoogleUserInfoResponse("google-sub",
				"google@test.com", true, "Google User", "https://image.test/profile.jpg"));
		given(memberRepository.findByProviderAndProviderId("google", "google-sub"))
			.willReturn(Optional.of(existingMember));

		sessionAuthService.loginWithGoogle("auth-code", "valid-state", request, response);

		verify(securityContextRepository).saveContext(any(), eq(request), eq(response));
	}

	@Test
	@DisplayName("구글 OAuth 콜백 - 신규 소셜 계정 생성")
	void loginWithGoogle_success_createMember() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute("GOOGLE_OAUTH_STATE", "valid-state");
		MockHttpServletResponse response = new MockHttpServletResponse();

		given(googleOAuthClient.getAccessToken(eq("auth-code"), any(GoogleOAuthProperties.class)))
			.willReturn(new GoogleTokenResponse("access-token"));
		given(googleOAuthClient.getUserInfo("access-token")).willReturn(new GoogleUserInfoResponse("google-sub-new",
				"new@test.com", true, "New User", "https://image.test/new.jpg"));
		given(memberRepository.findByProviderAndProviderId("google", "google-sub-new")).willReturn(Optional.empty());
		given(memberRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
		given(passwordEncoder.encode(any())).willReturn("encoded-password");

		Member savedMember = createMember(2L, "new@test.com", "google", "google-sub-new");
		given(memberRepository.save(any(Member.class))).willReturn(savedMember);

		sessionAuthService.loginWithGoogle("auth-code", "valid-state", request, response);

		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(memberCaptor.capture());
		assertThat(memberCaptor.getValue().getProvider()).isEqualTo("google");
		assertThat(memberCaptor.getValue().getProviderId()).isEqualTo("google-sub-new");
		verify(securityContextRepository).saveContext(any(), eq(request), eq(response));
	}

	@Test
	@DisplayName("구글 OAuth 콜백 - 동일 이메일 기존 계정 로그인")
	void loginWithGoogle_success_existingEmailMember() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute("GOOGLE_OAUTH_STATE", "valid-state");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Member existingEmailMember = createMember(3L, "legacy@test.com", "local", null);

		given(googleOAuthClient.getAccessToken(eq("auth-code"), any(GoogleOAuthProperties.class)))
			.willReturn(new GoogleTokenResponse("access-token"));
		given(googleOAuthClient.getUserInfo("access-token")).willReturn(new GoogleUserInfoResponse("google-sub-legacy",
				"legacy@test.com", true, "Legacy User", "https://image.test/legacy.jpg"));
		given(memberRepository.findByProviderAndProviderId("google", "google-sub-legacy")).willReturn(Optional.empty());
		given(memberRepository.findByEmail("legacy@test.com")).willReturn(Optional.of(existingEmailMember));

		sessionAuthService.loginWithGoogle("auth-code", "valid-state", request, response);

		verify(memberRepository, never()).save(any(Member.class));
		verify(securityContextRepository).saveContext(any(), eq(request), eq(response));
	}

	@Test
	@DisplayName("구글 OAuth 콜백 - next 경로를 복원한다")
	void loginWithGoogle_success_restoreNextPath() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute("GOOGLE_OAUTH_STATE", "valid-state");
		request.getSession().setAttribute("GOOGLE_OAUTH_NEXT", "/item/321");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Member existingMember = createMember(1L, "google@test.com", "google", "google-sub");

		given(googleOAuthClient.getAccessToken(eq("auth-code"), any(GoogleOAuthProperties.class)))
			.willReturn(new GoogleTokenResponse("access-token"));
		given(googleOAuthClient.getUserInfo("access-token")).willReturn(new GoogleUserInfoResponse("google-sub",
				"google@test.com", true, "Google User", "https://image.test/profile.jpg"));
		given(memberRepository.findByProviderAndProviderId("google", "google-sub"))
			.willReturn(Optional.of(existingMember));

		String next = sessionAuthService.loginWithGoogle("auth-code", "valid-state", request, response);

		assertThat(next).isEqualTo("/item/321");
		assertThat(request.getSession().getAttribute("GOOGLE_OAUTH_NEXT")).isNull();
	}

	private Member createMember(Long id, String email, String provider, String providerId) {
		Member member = Member.builder()
			.email(email)
			.password("encoded-password")
			.name("user")
			.nickname("user")
			.provider(provider)
			.providerId(providerId)
			.build();
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}

}
