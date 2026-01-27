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

	int createIfNotApproved(Long itemId, Long memberId);

	void delete(Reservation reservation);

	Optional<Reservation> findByItemIdAndMemberIdAndStatus(Long itemId, Long memberId, ReservationStatus status);

	Optional<Reservation> findByIdWithItemAndMember(Long id);

}
