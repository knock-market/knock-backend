package com.knock.storage.db.core.review;

import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.reservation.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE review SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Review extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id", nullable = false)
	private Member reviewer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewee_id", nullable = false)
	private Member reviewee;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private Integer score;

	private Review(Reservation reservation, Member reviewer, Member reviewee, String content, Integer score) {
		this.reservation = reservation;
		this.reviewer = reviewer;
		this.reviewee = reviewee;
		this.content = content;
		this.score = score;
	}

	public static Review create(Reservation reservation, Member reviewer, Member reviewee, String content,
			Integer score) {
		return new Review(reservation, reviewer, reviewee, content, score);
	}

}
