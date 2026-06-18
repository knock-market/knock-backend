package com.knock.core.domain.report;

import com.knock.core.domain.report.dto.ReportCreateData;
import com.knock.core.domain.report.dto.ReportResult;
import com.knock.core.enums.ReportReason;
import com.knock.core.enums.ReportTargetType;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.report.Report;
import com.knock.storage.db.core.report.ReportRepository;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.review.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.createItem;
import static com.knock.core.support.TestFixtures.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

	@InjectMocks
	private ReportService reportService;

	@Mock
	private ReportRepository reportRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Test
	@DisplayName("상품 신고 생성 성공")
	void createReport_success() {
		Member reporter = createMember(TEST_MEMBER_ID);
		Member seller = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
		Item item = createItem(TEST_ITEM_ID, seller);
		ReportCreateData data = new ReportCreateData(ReportTargetType.ITEM, TEST_ITEM_ID, ReportReason.PROHIBITED_ITEM,
				"Prohibited item suspected");

		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(reporter));
		given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
		given(reportRepository.findDuplicate(TEST_MEMBER_ID, ReportTargetType.ITEM, TEST_ITEM_ID,
				ReportReason.PROHIBITED_ITEM)).willReturn(Optional.empty());
		given(reportRepository.save(any(Report.class))).willAnswer(invocation -> {
			Report report = invocation.getArgument(0);
			ReflectionTestUtils.setField(report, "id", 10L);
			return report;
		});

		ReportResult result = reportService.createReport(TEST_MEMBER_ID, data);

		assertThat(result.reportId()).isEqualTo(10L);
		assertThat(result.status().name()).isEqualTo("RECEIVED");
		verify(reportRepository).save(any(Report.class));
	}

	@Test
	@DisplayName("실패 - 본인 상품 신고 차단")
	void createReport_failSelfItem() {
		Member reporter = createMember(TEST_MEMBER_ID);
		Item item = createItem(TEST_ITEM_ID, reporter);
		ReportCreateData data = new ReportCreateData(ReportTargetType.ITEM, TEST_ITEM_ID, ReportReason.PROHIBITED_ITEM,
				null);

		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(reporter));
		given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));

		assertThatThrownBy(() -> reportService.createReport(TEST_MEMBER_ID, data)).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.SELF_REPORT_NOT_ALLOWED);
	}

	@Test
	@DisplayName("실패 - 중복 신고 차단")
	void createReport_failDuplicate() {
		Member reporter = createMember(TEST_MEMBER_ID);
		Member seller = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
		Item item = createItem(TEST_ITEM_ID, seller);
		Report existing = Report.create(reporter, ReportTargetType.ITEM, TEST_ITEM_ID, ReportReason.PROHIBITED_ITEM,
				"first");
		ReportCreateData data = new ReportCreateData(ReportTargetType.ITEM, TEST_ITEM_ID, ReportReason.PROHIBITED_ITEM,
				"second");

		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(reporter));
		given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
		given(reportRepository.findDuplicate(TEST_MEMBER_ID, ReportTargetType.ITEM, TEST_ITEM_ID,
				ReportReason.PROHIBITED_ITEM)).willReturn(Optional.of(existing));

		assertThatThrownBy(() -> reportService.createReport(TEST_MEMBER_ID, data)).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_REPORT);
	}

	@Test
	@DisplayName("실패 - 존재하지 않는 신고 대상")
	void createReport_failMissingItem() {
		Member reporter = createMember(TEST_MEMBER_ID);
		ReportCreateData data = new ReportCreateData(ReportTargetType.ITEM, TEST_ITEM_ID, ReportReason.PROHIBITED_ITEM,
				null);

		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(reporter));
		given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> reportService.createReport(TEST_MEMBER_ID, data)).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.ITEM_NOT_FOUND);
	}

}
