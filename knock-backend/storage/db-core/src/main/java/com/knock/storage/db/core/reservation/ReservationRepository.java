package com.knock.storage.db.core.reservation;

import com.knock.core.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

	Reservation save(Reservation reservation);

	Optional<Reservation> findById(Long id);

	List<Reservation> findByItemId(Long itemId);

	List<Reservation> findByMemberId(Long memberId);

	boolean existsByItemIdAndStatus(Long itemId, ReservationStatus status);

	int countByItemIdAndStatus(Long itemId, ReservationStatus status);

	/**
	 * 아이템에 승인된 예약이 없을 때만 예약을 생성합니다. (Race Condition 방지)
	 * @return 생성된 행 수 (0 또는 1)
	 */
	int createIfNotApproved(Long itemId, Long memberId);

	void delete(Reservation reservation);

}
