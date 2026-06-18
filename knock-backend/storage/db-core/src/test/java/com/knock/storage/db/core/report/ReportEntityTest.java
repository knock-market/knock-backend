package com.knock.storage.db.core.report;

import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportStatus;
import com.knock.core.enums.ReportTargetType;
import com.knock.storage.db.core.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportEntityTest {

	@Test
	@DisplayName("신고 생성 시 기본 상태와 대상 정보를 저장한다")
	void createInitializesReport() {
		Member reporter = Member.create("reporter@test.com", "Reporter", "password", "reporter", "LOCAL");

		Report report = Report.create(reporter, ReportTargetType.ITEM, 10L, ReportReason.PROHIBITED_ITEM, "  danger  ");

		assertThat(report.getReporter()).isSameAs(reporter);
		assertThat(report.getTargetType()).isEqualTo(ReportTargetType.ITEM);
		assertThat(report.getTargetId()).isEqualTo(10L);
		assertThat(report.getReason()).isEqualTo(ReportReason.PROHIBITED_ITEM);
		assertThat(report.getDescription()).isEqualTo("danger");
		assertThat(report.getStatus()).isEqualTo(ReportStatus.RECEIVED);
	}

	@Test
	@DisplayName("신고 설명이 null이면 null로 유지한다")
	void createKeepsNullDescription() {
		Member reporter = Member.create("reporter-null@test.com", "Reporter", "password", "reporter", "LOCAL");

		Report report = Report.create(reporter, ReportTargetType.MEMBER, 20L, ReportReason.OTHER, null);

		assertThat(report.getDescription()).isNull();
	}

	@Test
	@DisplayName("신고 설명이 공백이면 null로 정규화한다")
	void createNormalizesBlankDescription() {
		Member reporter = Member.create("reporter-blank@test.com", "Reporter", "password", "reporter", "LOCAL");

		Report report = Report.create(reporter, ReportTargetType.REVIEW, 30L, ReportReason.HARASSMENT_OR_THREAT, "   ");

		assertThat(report.getDescription()).isNull();
	}

}
