package com.knock.core.api.controller.v1.response;

import com.knock.core.domain.report.dto.ReportResult;
import com.knock.core.enums.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponseDto(Long reportId, ReportStatus status, LocalDateTime createdAt) {

	public static ReportResponseDto from(ReportResult result) {
		return new ReportResponseDto(result.reportId(), result.status(), result.createdAt());
	}

}
