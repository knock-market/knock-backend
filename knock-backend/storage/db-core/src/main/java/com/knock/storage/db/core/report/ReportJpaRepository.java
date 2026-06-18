package com.knock.storage.db.core.report;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ReportJpaRepository extends JpaRepository<Report, Long> {

	Optional<Report> findByReporter_IdAndTargetTypeAndTargetIdAndReason(Long reporterId, ReportTargetType targetType,
			Long targetId, ReportReason reason);

}
