package com.knock.storage.db.core.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("local")
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	final String EMAIL = "EMAIL-id@example.com";

	final String NAME = "test-name";

	final String NICKNAME = "test-nickname";

	final String PROVIDER = "GOOGLE";

	final String PROFILE_IMAGE_URL = "http://profile.img/1";

	@Test
	void testShouldBeSavedAndFound() {
		// given
		Member member = Member.builder()
			.email(EMAIL)
			.name(NAME)
			.nickname(NICKNAME)
			.provider(PROVIDER)
			.profileImageUrl(PROFILE_IMAGE_URL)
			.build();

		// when
		Member savedMember = memberRepository.save(member);

		// then
		assertThat(savedMember.getId()).isNotNull();
		assertThat(savedMember.getName()).isEqualTo(NAME);
		assertThat(savedMember.getNickname()).isEqualTo(NICKNAME);
		assertThat(savedMember.getEmail()).isEqualTo(EMAIL);
	}

	@Test
	void testShouldBeSavedAndDeleted() {
		// given
		Member member = Member.builder()
			.email(EMAIL)
			.name(NAME)
			.nickname(NICKNAME)
			.provider(PROVIDER)
			.profileImageUrl(PROFILE_IMAGE_URL)
			.build();
		memberRepository.save(member);

		// when
		Member foundMember = memberRepository.findByEmail(EMAIL).orElseThrow();

		// then
		assertThat(foundMember.getName()).isEqualTo(NAME);
		foundMember.delete();
		memberRepository.save(foundMember);
		// @SQLRestriction 동작 테스트
		assertThat(memberRepository.findByEmail(EMAIL)).isEmpty();
	}

}