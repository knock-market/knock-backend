package com.knock.storage.db.core.seller;

import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seller_access_member",
		uniqueConstraints = @UniqueConstraint(name = "uk_seller_access_member_seller_member",
				columnNames = { "seller_id", "member_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerAccessMember extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seller_id", nullable = false)
	private Member seller;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_share_link_id")
	private SellerShareLink sourceShareLink;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SellerAccessMemberStatus status;

	private SellerAccessMember(Member seller, Member member, SellerShareLink sourceShareLink) {
		this.seller = seller;
		this.member = member;
		this.sourceShareLink = sourceShareLink;
		this.status = SellerAccessMemberStatus.ACTIVE;
	}

	public static SellerAccessMember create(Member seller, Member member, SellerShareLink sourceShareLink) {
		return new SellerAccessMember(seller, member, sourceShareLink);
	}

	public boolean isActive() {
		return status == SellerAccessMemberStatus.ACTIVE;
	}

}
