package com.knock.storage.db.core.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface MemberJpaRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByEmail(String email);

	Optional<Member> findByProviderAndProviderId(String provider, String providerId);

	boolean existsByEmail(String email);

}
