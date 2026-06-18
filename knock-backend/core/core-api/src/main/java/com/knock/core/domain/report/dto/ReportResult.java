package com.knock.core.domain.report.dto;

import com.knock.core.enums.ReportStatus;
import com.knock.storage.db.core.report.Report;

import java.time.LocalDateTime;

public record ReportResult(Long reportId, ReportStatus status, LocalDateTime createdAt) {

	public static ReportResult from(Report report) {
		return new ReportResult(report.getId(), report.getStatus(), report.getCreatedAt());
	}

}
