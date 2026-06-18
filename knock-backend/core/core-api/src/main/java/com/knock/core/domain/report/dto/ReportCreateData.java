package com.knock.core.domain.report.dto;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportTargetType;

public record ReportCreateData(ReportTargetType targetType, Long targetId, ReportReason reason, String description) {
}
