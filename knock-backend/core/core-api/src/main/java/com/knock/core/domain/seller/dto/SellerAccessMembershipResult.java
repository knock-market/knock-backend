package com.knock.core.domain.seller.dto;

import com.knock.storage.db.core.seller.SellerAccessMember;

import java.time.LocalDateTime;

public record SellerAccessMembershipResult(Long sellerId, Long memberId, String status, LocalDateTime createdAt) {

	public static SellerAccessMembershipResult from(SellerAccessMember accessMember) {
		return new SellerAccessMembershipResult(accessMember.getSeller().getId(), accessMember.getMember().getId(),
				accessMember.getStatus().name(), accessMember.getCreatedAt());
	}

}
