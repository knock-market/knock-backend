package com.knock.storage.db.core.member;

import com.knock.storage.db.core.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("회원 저장 및 조회 성공")
	void saveAndFindMember() {
		// given
		Member member = Member.create("test@test.com", "Name", "Password", "Nickname", "LOCAL");

		// when
		Member savedMember = memberRepository.save(member);
		Optional<Member> foundMember = memberRepository.findById(savedMember.getId());

		// then
		assertThat(foundMember).isPresent();
		assertThat(foundMember.get().getEmail()).isEqualTo("test@test.com");
	}

	@Test
	@DisplayName("이메일로 회원 존재 여부 확인")
	void existsByEmail() {
		// given
		Member member = Member.create("test@test.com", "Name", "Password", "Nickname", "LOCAL");
		memberRepository.save(member);

		// when
		boolean exists = memberRepository.existsByEmail("test@test.com");
		boolean notExists = memberRepository.existsByEmail("non-existent@test.com");

		// then
		assertThat(exists).isTrue();
		assertThat(notExists).isFalse();
	}
}