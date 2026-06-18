package com.knock.storage.db.core.block;

import com.knock.core.enums.BlockStatus;
import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "member_block", uniqueConstraints = { @UniqueConstraint(columnNames = { "blocker_id", "blocked_id" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE member_block SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MemberBlock extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocker_id", nullable = false)
	private Member blocker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "blocked_id", nullable = false)
	private Member blocked;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private BlockStatus status;

	private MemberBlock(Member blocker, Member blocked) {
		this.blocker = blocker;
		this.blocked = blocked;
		this.status = BlockStatus.ACTIVE;
	}

	public static MemberBlock create(Member blocker, Member blocked) {
		return new MemberBlock(blocker, blocked);
	}

}
