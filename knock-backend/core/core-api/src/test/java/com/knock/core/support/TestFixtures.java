package com.knock.core.support;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;
import com.knock.core.enums.ReservationStatus;
import com.knock.storage.db.core.bookmark.Bookmark;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.reservation.Reservation;
import org.springframework.test.util.ReflectionTestUtils;

import static com.knock.core.support.TestConstants.*;

/**
 * 테스트 객체 공통 생성
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Member createMember() {
        return createMember(TEST_MEMBER_ID);
    }

    public static Member createMember(Long id) {
        Member member = Member.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .nickname(TEST_NICKNAME)
                .provider(TEST_PROVIDER)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static Member createMember(Long id, String email) {
        Member member = Member.builder()
                .email(email)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .nickname(TEST_NICKNAME)
                .provider(TEST_PROVIDER)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static Group createGroup() {
        return createGroup(TEST_GROUP_ID, TEST_MEMBER_ID);
    }

    public static Group createGroup(Long id, Long ownerId) {
        Group group = Group.create(TEST_GROUP_NAME, TEST_GROUP_DESCRIPTION, TEST_INVITE_CODE, ownerId);
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    public static Item createItem(Long id, Group group, Member member) {
        Item item = Item.create(
                group,
                member,
                TEST_ITEM_TITLE,
                TEST_ITEM_DESCRIPTION,
                TEST_ITEM_PRICE,
                ItemType.SELL,
                ItemCategory.DIGITAL_DEVICE);
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    public static Bookmark createBookmark(Long id, Member member, Item item) {
        Bookmark bookmark = Bookmark.create(member, item);
        ReflectionTestUtils.setField(bookmark, "id", id);
        return bookmark;
    }

    public static Reservation createReservation(Long id, Item item, Member member) {
        Reservation reservation = Reservation.create(item, member);
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    public static Reservation createReservation(Long id, Item item, Member member, ReservationStatus status) {
        Reservation reservation = Reservation.create(item, member);
        ReflectionTestUtils.setField(reservation, "id", id);
        ReflectionTestUtils.setField(reservation, "status", status);
        return reservation;
    }
}
