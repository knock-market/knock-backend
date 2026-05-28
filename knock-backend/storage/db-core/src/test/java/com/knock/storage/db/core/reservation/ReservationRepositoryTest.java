package com.knock.storage.db.core.reservation;

import com.knock.core.enums.ItemType;
import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRepositoryTest extends CoreDbContextTest {

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Test
	@DisplayName("멤버별 예약 목록 조회")
	void findByMemberId() {
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = itemRepository.save(Item.create(member, "Title", "Desc", 1000L, ItemType.SELL), List.of());
		reservationRepository.save(Reservation.create(item, member));

		List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());

		assertThat(reservations).hasSize(1);
	}

	@Test
	@DisplayName("상품별 예약 목록 조회")
	void findByItemId() {
		Member member = memberRepository.save(Member.create("test@test.com", "Name", "Pass", "Nick", "LOCAL"));
		Item item = itemRepository.save(Item.create(member, "Title", "Desc", 1000L, ItemType.SELL), List.of());
		reservationRepository.save(Reservation.create(item, member));

		List<Reservation> reservations = reservationRepository.findByItemId(item.getId());

		assertThat(reservations).hasSize(1);
	}

}
