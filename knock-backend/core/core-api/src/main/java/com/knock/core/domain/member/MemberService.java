package com.knock.core.domain.member;

import com.knock.core.domain.member.dto.BlockedMemberResult;
import com.knock.core.domain.member.dto.MemberNotificationSettingsResult;
import com.knock.core.domain.member.dto.MemberNotificationSettingsUpdateData;
import com.knock.core.domain.member.dto.MemberResult;
import com.knock.core.domain.member.dto.MemberSignupData;
import com.knock.core.domain.member.dto.MemberSignupResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberBlock;
import com.knock.storage.db.core.member.MemberBlockRepository;
import com.knock.storage.db.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;

	private final MemberBlockRepository memberBlockRepository;

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

		return MemberSignupResult.of(saved);
	}

	@Transactional(readOnly = true)
	public MemberResult getMember(Long memberId) {
		Member member = getMemberOrThrow(memberId);
		return MemberResult.of(member);
	}

	@Transactional
	public void updateProfile(Long memberId, String nickname, String profileImageUrl) {
		Member member = getMemberOrThrow(memberId);

		if (nickname == null || nickname.isBlank()) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}

		String normalizedImageUrl = profileImageUrl;
		if (normalizedImageUrl != null && normalizedImageUrl.isBlank()) {
			normalizedImageUrl = null;
		}

		member.updateProfile(nickname.trim(), normalizedImageUrl);
	}

	@Transactional(readOnly = true)
	public MemberNotificationSettingsResult getNotificationSettings(Long memberId) {
		Member member = getMemberOrThrow(memberId);
		return MemberNotificationSettingsResult.from(member);
	}

	@Transactional
	public void updateNotificationSettings(Long memberId, MemberNotificationSettingsUpdateData data) {
		Member member = getMemberOrThrow(memberId);
		member.updateNotificationSettings(data.push(), data.newItems(), data.chat(), data.marketing(), data.sound());
	}

	@Transactional(readOnly = true)
	public List<BlockedMemberResult> getBlockedMembers(Long memberId) {
		getMemberOrThrow(memberId);
		return memberBlockRepository.findByBlockerId(memberId).stream().map(BlockedMemberResult::from).toList();
	}

	@Transactional
	public void blockMember(Long blockerId, Long blockedId) {
		if (blockerId.equals(blockedId)) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}

		Member blocker = getMemberOrThrow(blockerId);
		Member blocked = getMemberOrThrow(blockedId);

		if (memberBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
			return;
		}

		try {
			memberBlockRepository.save(MemberBlock.create(blocker, blocked));
		}
		catch (DataIntegrityViolationException e) {
			if (memberBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
				return;
			}
			throw e;
		}
	}

	@Transactional
	public void unblockMember(Long blockerId, Long blockedId) {
		getMemberOrThrow(blockerId);
		getMemberOrThrow(blockedId);
		memberBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
			.ifPresent(memberBlockRepository::delete);
	}

	private Member getMemberOrThrow(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
	}

}
