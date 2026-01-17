package com.knock.storage.db.core.group;

import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "group_member", uniqueConstraints = { @UniqueConstraint(columnNames = { "group_id", "member_id" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE group_member SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupMember extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GroupRole role; // ADMIN, MEMBER

	public enum GroupRole {
		ADMIN, MEMBER
	}

	@Builder
	private GroupMember(Group group, Member member, GroupRole role) {
		this.group = group;
		this.member = member;
		this.role = role;
	}

	public static GroupMember create(Group group, Member member, GroupRole role) {
		return GroupMember.builder().group(group).member(member).role(role).build();
	}

}