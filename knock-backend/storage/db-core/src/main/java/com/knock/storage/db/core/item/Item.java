package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE item SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Item extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
	private List<ItemImage> images = new ArrayList<>();

	@Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 36)
	private String publicId;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private Long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemStatus status;

	// todo: 조회 로직에 대해서 고민 e.g. 세션 기반으로 한 사용자당 30분마다 +1
	@Column(name = "view_count", nullable = false, columnDefinition = "bigint default 0")
	private Long viewCount;

	@Column(name = "trade_location_name")
	private String tradeLocationName;

	@Column(name = "trade_location_address")
	private String tradeLocationAddress;

	@Column(name = "trade_latitude")
	private Double tradeLatitude;

	@Column(name = "trade_longitude")
	private Double tradeLongitude;

	public Item(Member member, String title, String description, Long price, ItemType type) {
		this.member = member;
		this.publicId = UUID.randomUUID().toString();
		this.title = title;
		this.description = description;
		this.price = price;
		this.type = type;
		this.status = ItemStatus.ON_SALE;
		this.viewCount = 0L;
	}

	public static Item create(Member member, String title, String description, Long price, ItemType type) {
		return new Item(member, title, description, price, type);
	}

	public void updateTradeLocation(String locationName, String locationAddress, Double latitude, Double longitude) {
		this.tradeLocationName = locationName;
		this.tradeLocationAddress = locationAddress;
		this.tradeLatitude = latitude;
		this.tradeLongitude = longitude;
	}

	@PrePersist
	private void assignPublicId() {
		if (publicId == null || publicId.isBlank()) {
			publicId = UUID.randomUUID().toString();
		}
	}

}
