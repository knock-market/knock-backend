package com.knock.storage.db.core.seller;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerAccessMemberJpaRepository extends JpaRepository<SellerAccessMember, Long> {

	Optional<SellerAccessMember> findBySellerIdAndMemberId(Long sellerId, Long memberId);

	boolean existsBySellerIdAndMemberIdAndStatus(Long sellerId, Long memberId, SellerAccessMemberStatus status);

}
