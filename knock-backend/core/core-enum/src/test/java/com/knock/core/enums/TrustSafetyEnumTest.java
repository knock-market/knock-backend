package com.knock.core.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrustSafetyEnumTest {

	@Test
	@DisplayName("차단 상태 설명을 제공한다")
	void blockStatusDescription() {
		assertThat(BlockStatus.ACTIVE.getDescription()).isEqualTo("차단 중");
	}

	@Test
	@DisplayName("신고 대상 타입 설명을 제공한다")
	void reportTargetTypeDescriptions() {
		assertThat(ReportTargetType.MEMBER.getDescription()).isEqualTo("회원");
		assertThat(ReportTargetType.ITEM.getDescription()).isEqualTo("상품");
		assertThat(ReportTargetType.RESERVATION.getDescription()).isEqualTo("예약");
		assertThat(ReportTargetType.REVIEW.getDescription()).isEqualTo("후기");
	}

	@Test
	@DisplayName("신고 상태 설명을 제공한다")
	void reportStatusDescriptions() {
		assertThat(ReportStatus.RECEIVED.getDescription()).isEqualTo("접수됨");
		assertThat(ReportStatus.REVIEWING.getDescription()).isEqualTo("검토 중");
		assertThat(ReportStatus.RESOLVED.getDescription()).isEqualTo("처리 완료");
		assertThat(ReportStatus.DISMISSED.getDescription()).isEqualTo("기각됨");
	}

	@Test
	@DisplayName("신고 사유 설명을 제공한다")
	void reportReasonDescriptions() {
		assertThat(ReportReason.PROHIBITED_ITEM.getDescription()).isEqualTo("금지 품목");
		assertThat(ReportReason.SUSPECTED_FRAUD.getDescription()).isEqualTo("사기 의심");
		assertThat(ReportReason.OFF_PLATFORM_PAYMENT.getDescription()).isEqualTo("외부 결제 유도");
		assertThat(ReportReason.PERSONAL_INFO_OR_CODE_REQUEST.getDescription()).isEqualTo("개인정보 또는 인증코드 요구");
		assertThat(ReportReason.HARASSMENT_OR_THREAT.getDescription()).isEqualTo("괴롭힘 또는 위협");
		assertThat(ReportReason.NO_SHOW.getDescription()).isEqualTo("노쇼");
		assertThat(ReportReason.COUNTERFEIT_OR_STOLEN_SUSPECTED.getDescription()).isEqualTo("위조품 또는 도난품 의심");
		assertThat(ReportReason.OTHER.getDescription()).isEqualTo("기타");
	}

}
