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
		memberRepository.findById(data.memberId()).orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		itemRepository.findById(data.itemId()).orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		int created = reservationRepository.createIfNotApproved(data.itemId(), data.memberId());

		if (created == 0) {
			throw new CoreException(ErrorType.RESERVATION_ALREADY_EXISTS);
		}

		return reservationRepository
				.findByItemIdAndMemberIdAndStatus(data.itemId(), data.memberId(), ReservationStatus.WAITING)
				.map(Reservation::getId)
				.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));
	}

	@Transactional
	public void approveReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findByIdWithItemAndMember(reservationId)
				.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

		if (!reservation.getItem().getMember().getId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		reservation.approve();
	}

	@Transactional
	public void completeReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findByIdWithItemAndMember(reservationId)
				.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

		boolean isOwner = reservation.getItem().getMember().getId().equals(memberId);
		boolean isReserver = reservation.getMember().getId().equals(memberId);

		if (!isOwner && !isReserver) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		reservation.complete();
	}

	@Transactional
	public void cancelReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findByIdWithItemAndMember(reservationId)
				.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

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
