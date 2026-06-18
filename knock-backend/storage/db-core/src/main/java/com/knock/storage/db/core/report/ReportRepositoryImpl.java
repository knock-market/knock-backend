package com.knock.storage.db.core.report;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {

	private final ReportJpaRepository jpaRepository;

	@Override
	public Report save(Report report) {
		return jpaRepository.save(report);
	}

	@Override
	public Optional<Report> findDuplicate(Long reporterId, ReportTargetType targetType, Long targetId,
			ReportReason reason) {
		return jpaRepository.findByReporter_IdAndTargetTypeAndTargetIdAndReason(reporterId, targetType, targetId,
				reason);
	}

}
