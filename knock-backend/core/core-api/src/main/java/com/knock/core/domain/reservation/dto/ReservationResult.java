package com.knock.core.domain.reservation.dto;

import com.knock.core.enums.ReservationStatus;
import com.knock.storage.db.core.reservation.Reservation;

import java.time.LocalDateTime;

public record ReservationResult(Long id, Long itemId, String itemTitle, Long memberId, String memberName,
		ReservationStatus status, LocalDateTime createdAt) {

	public static ReservationResult from(Reservation reservation) {
		return new ReservationResult(reservation.getId(), reservation.getItem().getId(),
				reservation.getItem().getTitle(), reservation.getMember().getId(), reservation.getMember().getName(),
				reservation.getStatus(), reservation.getCreatedAt());
	}
}
