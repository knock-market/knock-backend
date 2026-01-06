package com.knock.auth;

import com.knock.auth.exception.AuthUserPasswordMismatchException;
import com.knock.auth.exception.MemberNameNotFoundException;
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

@Service
public class SessionAuthService implements AuthService {

	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

	private final SecurityContextRepository securityContextRepository;

	public SessionAuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
			SecurityContextRepository securityContextRepository) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.securityContextRepository = securityContextRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public void login(LoginRequestData data, HttpServletRequest request, HttpServletResponse response) {
		Member member = memberRepository.findByEmail(data.email).orElseThrow(MemberNameNotFoundException::new);

		if (!passwordEncoder.matches(data.rawPassword, member.getPassword())) {
			throw new AuthUserPasswordMismatchException();
		}

		MemberPrincipal principal = MemberPrincipal.from(member);
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null,
				principal.getAuthorities());

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);

		securityContextRepository.saveContext(securityContext, request, response);
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
}
