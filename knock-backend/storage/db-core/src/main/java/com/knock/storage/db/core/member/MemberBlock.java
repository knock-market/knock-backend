package com.knock.storage.db.core.member;

import com.knock.storage.db.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_block", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "blocker_id", "blocked_id" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlock extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocker_id", nullable = false)
	private Member blocker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocked_id", nullable = false)
	private Member blocked;

	@Builder
	private MemberBlock(Member blocker, Member blocked) {
		this.blocker = blocker;
		this.blocked = blocked;
	}

	public static MemberBlock create(Member blocker, Member blocked) {
		return MemberBlock.builder().blocker(blocker).blocked(blocked).build();
	}

}
