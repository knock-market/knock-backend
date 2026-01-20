package com.knock.storage.db.core.reservation;

import com.knock.core.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByItem_IdOrderByCreatedAtAsc(Long itemId);

	List<Reservation> findByMember_IdOrderByCreatedAtDesc(Long memberId);

	boolean existsByItem_IdAndStatus(Long itemId, ReservationStatus status);

	int countByItem_IdAndStatus(Long itemId, ReservationStatus status);

	@Modifying
	@Query(value = """
			INSERT INTO reservation (item_id, member_id, status, created_at, updated_at)
			SELECT :itemId, :memberId, 'WAITING', NOW(), NOW()
			WHERE NOT EXISTS (
			    SELECT 1 FROM reservation
			    WHERE item_id = :itemId
			      AND status = 'APPROVED'
			      AND deleted_at IS NULL
			)
			""", nativeQuery = true)
	int createReservationIfNotApproved(@Param("itemId") Long itemId, @Param("memberId") Long memberId);

	@Query("SELECT r FROM Reservation r WHERE r.item.id = :itemId AND r.member.id = :memberId AND r.status = :status")
	Optional<Reservation> findByItemIdAndMemberIdAndStatus(Long itemId, Long memberId,
														   ReservationStatus status);

	@Query("SELECT r FROM Reservation r JOIN FETCH r.item i JOIN FETCH r.member JOIN FETCH i.member WHERE r.id = :id")
	Optional<Reservation> findByIdWithItemAndMember(Long id);

}
