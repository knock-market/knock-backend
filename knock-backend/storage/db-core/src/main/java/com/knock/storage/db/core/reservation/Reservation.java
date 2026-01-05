package com.knock.storage.db.core.reservation;

import com.knock.core.enums.ReservationStatus;
import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE reservation SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Reservation extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member; // 예약 요청자

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status; // WAITING, APPROVED, COMPLETED, CANCELED

	private Reservation(Item item, Member member) {
		this.item = item;
		this.member = member;
		this.status = ReservationStatus.WAITING;
	}

	public static Reservation create(Item item, Member member) {
		return new Reservation(item, member);
	}

	public void approve() {
		if (this.status != ReservationStatus.WAITING) {
			throw new IllegalStateException("대기 중인 예약만 승인할 수 있습니다.");
		}
		this.status = ReservationStatus.APPROVED;
	}

	public void complete() {
		if (this.status != ReservationStatus.APPROVED) {
			throw new IllegalStateException("승인된 예약만 완료할 수 있습니다.");
		}
		this.status = ReservationStatus.COMPLETED;
	}

	public void cancel() {
		if (this.status == ReservationStatus.COMPLETED) {
			throw new IllegalStateException("완료된 예약은 취소할 수 없습니다.");
		}
		this.status = ReservationStatus.CANCELED;
	}

}