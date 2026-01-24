package com.knock.storage.db.core.reservation;

import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;
import com.knock.storage.db.core.config.QueryDslConfig;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @DisplayName("멤버별 예약 목록 조회")
    void findByMemberId() {
        // given
        Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
        Group group = groupRepository.save(Group.create("Group", "Desc", member));
        Item item = itemRepository.save(
                Item.create("Title", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member),
                List.of());
        reservationRepository.save(Reservation.create(item, member));

        // when
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());

        // then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("상품별 예약 목록 조회")
    void findByItemId() {
        // given
        Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
        Group group = groupRepository.save(Group.create("Group", "Desc", member));
        Item item = itemRepository.save(
                Item.create("Title", "Desc", 1000L, ItemType.SELL, ItemCategory.DIGITAL_DEVICE, group, member),
                List.of());
        reservationRepository.save(Reservation.create(item, member));

        // when
        List<Reservation> reservations = reservationRepository.findByItemId(item.getId());

        // then
        assertThat(reservations).hasSize(1);
    }
}
