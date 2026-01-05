package com.knock.storage.db.core.item;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemStatus;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.BaseEntity;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE item SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Item extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

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
	private ItemCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ItemStatus status;

	public Item(Group group, Member member, String title, String description, Long price, ItemType type,
			ItemCategory category) {
		this.group = group;
		this.member = member;
		this.title = title;
		this.description = description;
		this.price = price;
		this.type = type;
		this.category = category;
		this.status = ItemStatus.ON_SALE;
	}

	public static Item create(Group group, Member member, String title, String description, Long price, ItemType type,
			ItemCategory category) {
		return new Item(group, member, title, description, price, type, category);
	}

}