package com.knock.storage.db.core.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository jpaRepository;

	@Override
	public Member save(Member member) {
		return jpaRepository.save(member);
	}

	@Override
	public Optional<Member> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Optional<Member> findByEmail(String email) {
		return jpaRepository.findByEmail(email);
	}

	@Override
	public Optional<Member> findByProviderAndProviderId(String provider, String providerId) {
		return jpaRepository.findByProviderAndProviderId(provider, providerId);
	}

	@Override
	public boolean existsByEmail(String email) {
		return jpaRepository.existsByEmail(email);
	}

	@Override
	public boolean existsById(Long id) {
		return jpaRepository.existsById(id);
	}

}
