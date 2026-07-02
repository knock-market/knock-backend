package com.knock.core.domain.item;

import com.knock.core.enums.ReservationStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemDeleteServiceTest {

	@InjectMocks
	private ItemService itemService;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	@DisplayName("성공")
	void success() {
		// given
		Member member = createMember(TEST_MEMBER_ID);
		Member reserver = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
		Item item = createItem(TEST_ITEM_ID, member);
		Reservation waitingReservation = createReservation(TEST_RESERVATION_ID, item, reserver);
		Reservation approvedReservation = createReservation(TEST_RESERVATION_ID + 1, item, reserver,
				ReservationStatus.APPROVED);
		Reservation completedReservation = createReservation(TEST_RESERVATION_ID + 2, item, reserver,
				ReservationStatus.COMPLETED);

		given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
		given(reservationRepository.findByItemId(TEST_ITEM_ID))
			.willReturn(List.of(waitingReservation, approvedReservation, completedReservation));

		// when
		itemService.deleteItem(TEST_MEMBER_ID, TEST_ITEM_ID);

		// then
		assertThat(waitingReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
		assertThat(approvedReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
		assertThat(completedReservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
		verify(itemRepository).delete(item);
	}

	@Test
	@DisplayName("실패 - 작성자가 아니면 삭제 불가")
	void fail_nonOwnerCannotDelete() {
		// given
		Member owner = createMember(TEST_MEMBER_ID);
		Item item = createItem(TEST_ITEM_ID, owner);

		given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));

		// when & then
		assertThatThrownBy(() -> itemService.deleteItem(TEST_MEMBER_ID_2, TEST_ITEM_ID))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		verify(reservationRepository, never()).findByItemId(TEST_ITEM_ID);
		verify(itemRepository, never()).delete(item);
	}

}
