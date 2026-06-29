package com.knock.core.domain.reservation;

import com.knock.core.domain.block.BlockService;
import com.knock.core.domain.notification.NotificationService;
import com.knock.core.domain.notification.dto.NotificationCreateData;
import com.knock.core.domain.reservation.dto.ReservationCreateData;
import com.knock.core.domain.reservation.dto.ReservationResult;
import com.knock.core.enums.NotificationType;
import com.knock.core.enums.ReservationStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
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

	private final NotificationService notificationService;

	private final BlockService blockService;

	private final SellerAccessMemberRepository sellerAccessMemberRepository;

	@Transactional
	public Long createReservation(ReservationCreateData data) {
		Member requester = memberRepository.findById(data.memberId())
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		Item item = itemRepository.findById(data.itemId())
			.orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));
		if (item.getMember().getId().equals(requester.getId())) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
		validateSellerAccess(requester.getId(), item.getMember().getId());
		blockService.validateInteractionAllowed(requester.getId(), item.getMember().getId());

		int created = reservationRepository.createIfNotApproved(data.itemId(), data.memberId());

		if (created == 0) {
			throw new CoreException(ErrorType.RESERVATION_ALREADY_EXISTS);
		}

		Long reservationId = reservationRepository
			.findByItemIdAndMemberIdAndStatus(data.itemId(), data.memberId(), ReservationStatus.WAITING)
			.map(Reservation::getId)
			.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

		notifyCounterparty(item.getMember().getId(), data.memberId(), NotificationType.RESERVATION_CREATED,
				requester.getNickname() + "님이 '" + item.getTitle() + "' 상품에 관심을 보냈습니다.",
				"/manage-item/" + item.getId());
		return reservationId;
	}

	@Transactional
	public void approveReservation(Long memberId, Long reservationId) {
		Reservation reservation = reservationRepository.findByIdWithItemAndMember(reservationId)
			.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

		if (!reservation.getItem().getMember().getId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		List<Reservation> reservations = reservationRepository.findByItemIdForUpdate(reservation.getItem().getId());
		boolean hasAnotherApproved = reservations.stream()
			.anyMatch(each -> !each.getId().equals(reservationId) && each.getStatus() == ReservationStatus.APPROVED);
		if (hasAnotherApproved) {
			throw new CoreException(ErrorType.RESERVATION_ALREADY_EXISTS);
		}

		Reservation target = reservations.stream()
			.filter(each -> each.getId().equals(reservationId))
			.findFirst()
			.orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

		if (target.getStatus() != ReservationStatus.WAITING) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}

		blockService.validateInteractionAllowed(memberId, target.getMember().getId());
		target.approve();
		notifyCounterparty(target.getMember().getId(), memberId, NotificationType.RESERVATION_APPROVED,
				"'" + target.getItem().getTitle() + "' 예약이 승인되었습니다.", "/item/" + target.getItem().getPublicId());
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
		Long counterpartyId = isOwner ? reservation.getMember().getId() : reservation.getItem().getMember().getId();
		notifyCounterparty(counterpartyId, memberId, NotificationType.RESERVATION_COMPLETED,
				"'" + reservation.getItem().getTitle() + "' 거래가 완료되었습니다.",
				"/item/" + reservation.getItem().getPublicId());
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
		Long counterpartyId = isOwner ? reservation.getMember().getId() : reservation.getItem().getMember().getId();
		notifyCounterparty(counterpartyId, memberId, NotificationType.RESERVATION_CANCELED,
				"'" + reservation.getItem().getTitle() + "' 예약이 취소되었습니다.",
				"/item/" + reservation.getItem().getPublicId());
	}

	private void validateSellerAccess(Long memberId, Long sellerId) {
		if (!sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(sellerId, memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
	}

	public List<ReservationResult> getReservationsByItem(Long memberId, Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));
		if (!item.getMember().getId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}

		List<Reservation> reservations = reservationRepository.findByItemId(itemId);
		return reservations.stream().map(ReservationResult::from).collect(Collectors.toList());
	}

	public List<ReservationResult> getMyReservations(Long memberId) {
		List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
		return reservations.stream().map(ReservationResult::from).collect(Collectors.toList());
	}

	private void notifyCounterparty(Long targetMemberId, Long actorMemberId, NotificationType type, String content,
			String relatedUrl) {
		if (targetMemberId.equals(actorMemberId)) {
			return;
		}

		notificationService.createNotification(new NotificationCreateData(targetMemberId, type, content, relatedUrl));
	}

}
