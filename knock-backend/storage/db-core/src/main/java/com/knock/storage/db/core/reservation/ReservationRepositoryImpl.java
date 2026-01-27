package com.knock.storage.db.core.reservation;

import com.knock.core.enums.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

	private final ReservationJpaRepository jpaRepository;

	@Override
	public Reservation save(Reservation reservation) {
		return jpaRepository.save(reservation);
	}

	@Override
	public Optional<Reservation> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<Reservation> findByItemId(Long itemId) {
		return jpaRepository.findByItem_IdOrderByCreatedAtAsc(itemId);
	}

	@Override
	public List<Reservation> findByMemberId(Long memberId) {
		return jpaRepository.findByMember_IdOrderByCreatedAtDesc(memberId);
	}

	@Override
	public boolean existsByItemIdAndStatus(Long itemId, ReservationStatus status) {
		return jpaRepository.existsByItem_IdAndStatus(itemId, status);
	}

	@Override
	public int countByItemIdAndStatus(Long itemId, ReservationStatus status) {
		return jpaRepository.countByItem_IdAndStatus(itemId, status);
	}

	@Override
	public int createIfNotApproved(Long itemId, Long memberId) {
		return jpaRepository.createReservationIfNotApproved(itemId, memberId);
	}

	@Override
	public void delete(Reservation reservation) {
		jpaRepository.delete(reservation);
	}

	@Override
	public Optional<Reservation> findByItemIdAndMemberIdAndStatus(Long itemId, Long memberId,
			ReservationStatus status) {
		return jpaRepository.findByItemIdAndMemberIdAndStatus(itemId, memberId, status);
	}

	@Override
	public Optional<Reservation> findByIdWithItemAndMember(Long id) {
		return jpaRepository.findByIdWithItemAndMember(id);
	}

}
