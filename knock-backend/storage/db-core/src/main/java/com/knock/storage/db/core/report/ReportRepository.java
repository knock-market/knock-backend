package com.knock.storage.db.core.report;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportTargetType;

import java.util.Optional;

public interface ReportRepository {

	Report save(Report report);

	Optional<Report> findDuplicate(Long reporterId, ReportTargetType targetType, Long targetId, ReportReason reason);

}
