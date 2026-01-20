package com.knock.core.domain.member;

import com.knock.core.domain.member.dto.MemberResult;
import com.knock.core.domain.member.dto.MemberSignupData;
import com.knock.core.domain.member.dto.MemberSignupResult;
import com.knock.core.domain.member.event.MemberCreatedEvent;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public MemberSignupResult signup(MemberSignupData data) {
		if (memberRepository.existsByEmail(data.email())) {
			throw new CoreException(ErrorType.DUPLICATE_EMAIL);
		}

		Member member = Member.builder()
				.email(data.email())
				.password(passwordEncoder.encode(data.password()))
				.name(data.name())
				.nickname(data.nickname())
				.profileImageUrl(data.profileImageUrl())
				.provider(data.provider())
				.build();

		Member saved = memberRepository.save(member);

		eventPublisher.publishEvent(new MemberCreatedEvent(saved.getId(), saved.getName()));
		return MemberSignupResult.of(saved);
	}

	@Transactional(readOnly = true)
	public MemberResult getMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		return MemberResult.of(member);
	}

}
