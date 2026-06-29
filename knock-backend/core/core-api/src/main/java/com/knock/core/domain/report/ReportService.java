package com.knock.core.domain.report;

import com.knock.core.domain.report.dto.ReportCreateData;
import com.knock.core.domain.report.dto.ReportResult;
import com.knock.core.enums.ReportTargetType;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.report.Report;
import com.knock.storage.db.core.report.ReportRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.review.Review;
import com.knock.storage.db.core.review.ReviewRepository;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

	private final ReportRepository reportRepository;

	private final MemberRepository memberRepository;

	private final ItemRepository itemRepository;

	private final ReservationRepository reservationRepository;

	private final ReviewRepository reviewRepository;

	private final SellerAccessMemberRepository sellerAccessMemberRepository;

	@Transactional
	public ReportResult createReport(Long reporterId, ReportCreateData data) {
		Member reporter = memberRepository.findById(reporterId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		validateTargetExistsAndNotSelf(reporterId, data.targetType(), data.targetId());
		reportRepository.findDuplicate(reporterId, data.targetType(), data.targetId(), data.reason())
			.ifPresent(report -> {
				throw new CoreException(ErrorType.DUPLICATE_REPORT);
			});

		try {
			Report saved = reportRepository
				.save(Report.create(reporter, data.targetType(), data.targetId(), data.reason(), data.description()));
			return ReportResult.from(saved);
		}
		catch (DataIntegrityViolationException e) {
			throw new CoreException(ErrorType.DUPLICATE_REPORT);
		}
	}

	private void validateTargetExistsAndNotSelf(Long reporterId, ReportTargetType targetType, Long targetId) {
		switch (targetType) {
			case MEMBER -> validateMemberTarget(reporterId, targetId);
			case ITEM -> validateItemTarget(reporterId, targetId);
			case RESERVATION -> validateReservationTarget(reporterId, targetId);
			case REVIEW -> validateReviewTarget(reporterId, targetId);
		}
	}

	private void validateMemberTarget(Long reporterId, Long targetId) {
		Member target = memberRepository.findById(targetId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		if (target.getId().equals(reporterId)) {
			throw new CoreException(ErrorType.SELF_REPORT_NOT_ALLOWED);
		}
	}

	private void validateItemTarget(Long reporterId, Long targetId) {
		Item target = itemRepository.findById(targetId).orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));
		if (target.getMember().getId().equals(reporterId)) {
			throw new CoreException(ErrorType.SELF_REPORT_NOT_ALLOWED);
		}
		validateSellerAccess(reporterId, target.getMember().getId());
	}

	private void validateSellerAccess(Long memberId, Long sellerId) {
		if (!sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(sellerId, memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
	}

	private void validateReservationTarget(Long reporterId, Long targetId) {
		Reservation target = reservationRepository.findByIdWithItemAndMember(targetId)
			.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));
		boolean isSeller = target.getItem().getMember().getId().equals(reporterId);
		boolean isRequester = target.getMember().getId().equals(reporterId);
		if (isSeller || isRequester) {
			throw new CoreException(ErrorType.SELF_REPORT_NOT_ALLOWED);
		}
	}

	private void validateReviewTarget(Long reporterId, Long targetId) {
		Review target = reviewRepository.findById(targetId).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
		if (target.getReviewer().getId().equals(reporterId)) {
			throw new CoreException(ErrorType.SELF_REPORT_NOT_ALLOWED);
		}
	}

}
