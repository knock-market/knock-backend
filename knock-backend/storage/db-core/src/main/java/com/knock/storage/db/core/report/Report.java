package com.knock.storage.db.core.report;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportStatus;
import com.knock.core.enums.ReportTargetType;
import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "report", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "reporter_id", "target_type", "target_id", "reason" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE report SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Report extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id", nullable = false)
	private Member reporter;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 32)
	private ReportTargetType targetType;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private ReportReason reason;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ReportStatus status;

	private Report(Member reporter, ReportTargetType targetType, Long targetId, ReportReason reason, String description) {
		this.reporter = reporter;
		this.targetType = targetType;
		this.targetId = targetId;
		this.reason = reason;
		this.description = description;
		this.status = ReportStatus.RECEIVED;
	}

	public static Report create(Member reporter, ReportTargetType targetType, Long targetId, ReportReason reason,
			String description) {
		return new Report(reporter, targetType, targetId, reason, normalizeDescription(description));
	}

	private static String normalizeDescription(String description) {
		if (description == null || description.isBlank()) {
			return null;
		}
		return description.strip();
	}

}
