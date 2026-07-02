package com.knock.core.domain.seller;

import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerAccessPolicy {

	private final SellerAccessMemberRepository sellerAccessMemberRepository;

	public void validateAccessMember(Long memberId, Long sellerId) {
		if (!sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(sellerId, memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
	}

	public void validateOwnerOrAccessMember(Long memberId, Long sellerId) {
		if (sellerId.equals(memberId)) {
			return;
		}
		validateAccessMember(memberId, sellerId);
	}

}
