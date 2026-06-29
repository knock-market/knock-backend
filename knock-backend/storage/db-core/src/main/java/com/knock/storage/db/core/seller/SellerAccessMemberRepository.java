package com.knock.storage.db.core.seller;

import java.util.Optional;

public interface SellerAccessMemberRepository {

	SellerAccessMember save(SellerAccessMember sellerAccessMember);

	Optional<SellerAccessMember> findBySellerIdAndMemberId(Long sellerId, Long memberId);

	boolean existsActiveBySellerIdAndMemberId(Long sellerId, Long memberId);

}
