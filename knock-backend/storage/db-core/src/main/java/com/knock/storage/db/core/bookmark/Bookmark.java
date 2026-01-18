package com.knock.storage.db.core.bookmark;

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
@Table(name = "bookmark", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "member_id", "item_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE bookmark SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Bookmark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Bookmark(Member member, Item item) {
        this.member = member;
        this.item = item;
    }

    public static Bookmark create(Member member, Item item) {
        return new Bookmark(member, item);
    }

}
