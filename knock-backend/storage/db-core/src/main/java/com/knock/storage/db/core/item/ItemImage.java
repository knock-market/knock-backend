package com.knock.storage.db.core.item;

import com.knock.storage.db.core.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "item_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE item_image SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ItemImage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Column(name = "order_sequence")
	private int orderSequence;

	public ItemImage(Item item, String imageUrl, int orderSequence) {
		this.item = item;
		this.imageUrl = imageUrl;
		this.orderSequence = orderSequence;
	}

	public static ItemImage create(Item item, String imageUrl, int orderSequence) {
		return new ItemImage(item, imageUrl, orderSequence);
	}

}