package com.knock.storage.db.core.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerAccessMemberRepositoryImpl implements SellerAccessMemberRepository {

	private final SellerAccessMemberJpaRepository sellerAccessMemberJpaRepository;

	@Override
	public SellerAccessMember save(SellerAccessMember sellerAccessMember) {
		return sellerAccessMemberJpaRepository.save(sellerAccessMember);
	}

	@Override
	public Optional<SellerAccessMember> findBySellerIdAndMemberId(Long sellerId, Long memberId) {
		return sellerAccessMemberJpaRepository.findBySellerIdAndMemberId(sellerId, memberId);
	}

	@Override
	public boolean existsActiveBySellerIdAndMemberId(Long sellerId, Long memberId) {
		return sellerAccessMemberJpaRepository.existsBySellerIdAndMemberIdAndStatus(sellerId, memberId,
				SellerAccessMemberStatus.ACTIVE);
	}

}
