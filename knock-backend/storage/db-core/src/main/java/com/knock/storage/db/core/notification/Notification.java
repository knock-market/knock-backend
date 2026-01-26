package com.knock.storage.db.core.notification;

import com.knock.core.enums.NotificationType;
import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE notification SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Notification extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false)
	private NotificationType notificationType;

	@Column(nullable = false)
	private String content;

	@Column(name = "related_url")
	private String relatedUrl;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	private Notification(Member member, NotificationType notificationType, String content, String relatedUrl) {
		this.member = member;
		this.notificationType = notificationType;
		this.content = content;
		this.relatedUrl = relatedUrl;
		this.isRead = false;
	}

	public static Notification create(Member member, NotificationType notificationType, String content,
			String relatedUrl) {
		return new Notification(member, notificationType, content, relatedUrl);
	}

	public void markAsRead() {
		this.isRead = true;
	}

	public boolean isOwner(Long memberId) {
		return !member.getId().equals(memberId);
	}

}