package com.knock.core.domain.reservation;

import com.knock.core.domain.reservation.dto.ReservationCreateData;
import com.knock.core.domain.reservation.dto.ReservationResult;
import com.knock.core.enums.ReservationStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

	private final ReservationRepository reservationRepository;

	private final ItemRepository itemRepository;

	private final MemberRepository memberRepository;

	@Transactional
	public Long createReservation(ReservationCreateData data) {
		// Member validation
		memberRepository.findById(data.memberId()).orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		// Item validation
		itemRepository.findById(data.itemId()).orElseThrow(() -> new IllegalArgumentException("Item not found"));

		// Atomic operation: 승인된 예약이 없을 때만 생성 (Race Condition 방지)
		int created = reservationRepository.createIfNotApproved(data.itemId(), data.memberId());

		if (created == 0) {
			throw new IllegalStateException("이미 예약이 확정된 물건입니다.");
		}

		// 생성된 예약 조회하여 ID 반환
		// Note: Native query로 생성했으므로 별도 조회 필요
		List<Reservation> reservations = reservationRepository.findByItemId(data.itemId());
		return reservations.stream()
			.filter(r -> r.getMember().getId().equals(data.memberId()))
			.filter(r -> r.getStatus() == ReservationStatus.WAITING)
			.findFirst()
			.map(Reservation::getId)
			.orElseThrow(() -> new IllegalStateException("예약 생성 실패"));
	}

	@Transactional
	public void approveReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

		// 물건 주인인지 확인
		if (!reservation.getItem().getMember().getId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		reservation.approve();
	}

	@Transactional
	public void completeReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

		// 물건 주인 또는 예약자인지 확인
		boolean isOwner = reservation.getItem().getMember().getId().equals(memberId);
		boolean isReserver = reservation.getMember().getId().equals(memberId);

		if (!isOwner && !isReserver) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		reservation.complete();
	}

	@Transactional
	public void cancelReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

		// 물건 주인 또는 예약자인지 확인
		boolean isOwner = reservation.getItem().getMember().getId().equals(memberId);
		boolean isReserver = reservation.getMember().getId().equals(memberId);

		if (!isOwner && !isReserver) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		reservation.cancel();
	}

	public List<ReservationResult> getReservationsByItem(Long itemId) {
		List<Reservation> reservations = reservationRepository.findByItemId(itemId);
		return reservations.stream().map(ReservationResult::from).collect(Collectors.toList());
	}

	public List<ReservationResult> getMyReservations(Long memberId) {
		List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
		return reservations.stream().map(ReservationResult::from).collect(Collectors.toList());
	}

}
