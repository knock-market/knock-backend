package com.knock.storage.db.core.group;

import com.knock.storage.db.core.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE tb_group SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Group extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "invite_code", nullable = false, unique = true)
	private String inviteCode;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId; // 그룹장 ID

	@Column(name = "is_personal", nullable = false)
	private boolean isPersonal;

	@Column(name = "invite_code_expires_at")
	private LocalDateTime inviteCodeExpiresAt;

	private Group(String name, String description, String inviteCode, Long ownerId, boolean isPersonal) {
		this.name = name;
		this.description = description;
		this.inviteCode = inviteCode;
		this.ownerId = ownerId;
		this.isPersonal = isPersonal;
	}

	public static Group create(String name, String description, String inviteCode, Long ownerId) {
		return new Group(name, description, inviteCode, ownerId, false);
	}

	public static Group createPersonal(String name, String description, String inviteCode, Long ownerId) {
		return new Group(name, description, inviteCode, ownerId, true);
	}

	public void updateInviteCode(String inviteCode, LocalDateTime expiresAt) {
		this.inviteCode = inviteCode;
		this.inviteCodeExpiresAt = expiresAt;
	}

	public boolean isInviteCodeExpired() {
		return inviteCodeExpiresAt != null && inviteCodeExpiresAt.isBefore(LocalDateTime.now());
	}

}