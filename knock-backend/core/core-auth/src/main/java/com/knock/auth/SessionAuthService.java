package com.knock.auth;

import com.knock.auth.exception.AuthUserPasswordMismatchException;
import com.knock.auth.exception.MemberNameNotFoundException;
import com.knock.auth.google.GoogleOAuthClient;
import com.knock.auth.google.GoogleOAuthClient.GoogleTokenResponse;
import com.knock.auth.google.GoogleOAuthClient.GoogleUserInfoResponse;
import com.knock.auth.google.GoogleOAuthProperties;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class SessionAuthService implements AuthService {

	private static final String GOOGLE_PROVIDER = "google";

	private static final String GOOGLE_SCOPE = "openid email profile";

	private static final String GOOGLE_AUTHORIZATION_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";

	private static final String GOOGLE_OAUTH_STATE_KEY = "GOOGLE_OAUTH_STATE";

	private static final String GOOGLE_OAUTH_NEXT_KEY = "GOOGLE_OAUTH_NEXT";

	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

	private final SecurityContextRepository securityContextRepository;

	private final GoogleOAuthProperties googleOAuthProperties;

	private final GoogleOAuthClient googleOAuthClient;

	public SessionAuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
			SecurityContextRepository securityContextRepository, GoogleOAuthProperties googleOAuthProperties,
			GoogleOAuthClient googleOAuthClient) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.securityContextRepository = securityContextRepository;
		this.googleOAuthProperties = googleOAuthProperties;
		this.googleOAuthClient = googleOAuthClient;
	}

	@Override
	@Transactional(readOnly = true)
	public void login(LoginRequestData data, HttpServletRequest request, HttpServletResponse response) {
		Member member = memberRepository.findByEmail(data.email).orElseThrow(MemberNameNotFoundException::new);

		if (!passwordEncoder.matches(data.rawPassword, member.getPassword())) {
			throw new AuthUserPasswordMismatchException();
		}
		authenticate(member, request, response);
	}

	@Override
	public String getGoogleAuthorizationUrl(String next, HttpServletRequest request) {
		validateGoogleConfigured();

		HttpSession session = request.getSession(true);
		String state = UUID.randomUUID().toString();
		session.setAttribute(GOOGLE_OAUTH_STATE_KEY, state);
		session.setAttribute(GOOGLE_OAUTH_NEXT_KEY, normalizeNextPath(next));

		return UriComponentsBuilder.fromHttpUrl(GOOGLE_AUTHORIZATION_ENDPOINT)
			.queryParam("response_type", "code")
			.queryParam("client_id", googleOAuthProperties.getClientId())
			.queryParam("redirect_uri", googleOAuthProperties.getRedirectUri())
			.queryParam("scope", GOOGLE_SCOPE)
			.queryParam("state", state)
			.build()
			.toUriString();
	}

	@Override
	@Transactional
	public String loginWithGoogle(String code, String state, HttpServletRequest request, HttpServletResponse response) {
		validateGoogleConfigured();
		validateOauthState(request, state);

		GoogleUserInfoResponse userInfo = getGoogleUserInfo(code);
		Member member = resolveOrCreateGoogleMember(userInfo);
		authenticate(member, request, response);
		return consumeNextPath(request);
	}

	@Override
	public void logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
	}

	public record LoginRequestData(String email, String rawPassword) {
	}

	private void authenticate(Member member, HttpServletRequest request, HttpServletResponse response) {
		MemberPrincipal principal = MemberPrincipal.from(member);
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null,
				principal.getAuthorities());
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
		securityContextRepository.saveContext(securityContext, request, response);
	}

	private GoogleUserInfoResponse getGoogleUserInfo(String code) {
		try {
			GoogleTokenResponse token = googleOAuthClient.getAccessToken(code, googleOAuthProperties);
			if (token == null || token.access_token() == null || token.access_token().isBlank()) {
				throw new ResponseStatusException(UNAUTHORIZED, "Google access token 발급에 실패했습니다.");
			}
			GoogleUserInfoResponse userInfo = googleOAuthClient.getUserInfo(token.access_token());
			if (userInfo == null || userInfo.sub() == null || userInfo.sub().isBlank() || userInfo.email() == null
					|| userInfo.email().isBlank()) {
				throw new ResponseStatusException(UNAUTHORIZED, "Google 사용자 정보 조회에 실패했습니다.");
			}
			if (!Boolean.TRUE.equals(userInfo.email_verified())) {
				throw new ResponseStatusException(UNAUTHORIZED, "Google 이메일 인증이 필요합니다.");
			}
			return userInfo;
		}
		catch (RestClientException e) {
			throw new ResponseStatusException(UNAUTHORIZED, "Google OAuth 처리 중 오류가 발생했습니다.", e);
		}
	}

	private Member resolveOrCreateGoogleMember(GoogleUserInfoResponse userInfo) {
		return memberRepository.findByProviderAndProviderId(GOOGLE_PROVIDER, userInfo.sub())
			.orElseGet(
					() -> memberRepository.findByEmail(userInfo.email()).orElseGet(() -> createGoogleMember(userInfo)));
	}

	private Member createGoogleMember(GoogleUserInfoResponse userInfo) {
		String fallbackName = extractNameFromEmail(userInfo.email());
		String name = normalizeText(userInfo.name(), fallbackName);
		String nickname = normalizeText(userInfo.name(), fallbackName);
		String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

		Member member = Member.builder()
			.email(userInfo.email())
			.password(encodedPassword)
			.name(name)
			.nickname(nickname)
			.profileImageUrl(userInfo.picture())
			.provider(GOOGLE_PROVIDER)
			.providerId(userInfo.sub())
			.build();
		return memberRepository.save(member);
	}

	private void validateOauthState(HttpServletRequest request, String state) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			throw new ResponseStatusException(BAD_REQUEST, "OAuth state가 유효하지 않습니다.");
		}

		String expectedState = (String) session.getAttribute(GOOGLE_OAUTH_STATE_KEY);
		session.removeAttribute(GOOGLE_OAUTH_STATE_KEY);

		if (expectedState == null || state == null || state.isBlank() || !expectedState.equals(state)) {
			throw new ResponseStatusException(BAD_REQUEST, "OAuth state가 유효하지 않습니다.");
		}
	}

	private String consumeNextPath(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		Object next = session.getAttribute(GOOGLE_OAUTH_NEXT_KEY);
		session.removeAttribute(GOOGLE_OAUTH_NEXT_KEY);
		return next instanceof String ? normalizeNextPath((String) next) : null;
	}

	private void validateGoogleConfigured() {
		if (!googleOAuthProperties.isConfigured()) {
			throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Google OAuth 설정이 누락되었습니다.");
		}
	}

	private String normalizeText(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	private String extractNameFromEmail(String email) {
		int atIndex = email.indexOf('@');
		return atIndex > 0 ? email.substring(0, atIndex) : "google-user";
	}

	private String normalizeNextPath(String next) {
		if (next == null || next.isBlank()) {
			return null;
		}
		String trimmed = next.trim();
		if (!trimmed.startsWith("/") || trimmed.startsWith("//")) {
			return null;
		}
		if (trimmed.contains("\r") || trimmed.contains("\n")) {
			return null;
		}
		return trimmed;
	}

}
