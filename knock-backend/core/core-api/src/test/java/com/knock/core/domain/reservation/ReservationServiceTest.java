package com.knock.core.domain.reservation;

import com.knock.core.domain.notification.NotificationService;
import com.knock.core.domain.notification.dto.NotificationCreateData;
import com.knock.core.domain.reservation.dto.ReservationCreateData;
import com.knock.core.domain.reservation.dto.ReservationResult;
import com.knock.core.enums.NotificationType;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private NotificationService notificationService;

	@Nested
	@DisplayName("예약 생성")
	class CreateReservation {

		@Test
		@DisplayName("성공")
		void success_reservation() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, createGroup(), createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2));
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, member);
			ReservationCreateData data = new ReservationCreateData(TEST_ITEM_ID, TEST_MEMBER_ID);

			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
			given(reservationRepository.createIfNotApproved(TEST_ITEM_ID, TEST_MEMBER_ID)).willReturn(1);
			given(reservationRepository.findByItemIdAndMemberIdAndStatus(TEST_ITEM_ID, TEST_MEMBER_ID,
					ReservationStatus.WAITING))
				.willReturn(Optional.of(reservation));

			// when
			Long result = reservationService.createReservation(data);

			// then
			assertThat(result).isEqualTo(TEST_RESERVATION_ID);
			ArgumentCaptor<NotificationCreateData> notificationCaptor = ArgumentCaptor
				.forClass(NotificationCreateData.class);
			verify(notificationService).createNotification(notificationCaptor.capture());
			NotificationCreateData notification = notificationCaptor.getValue();
			assertThat(notification.memberId()).isEqualTo(TEST_MEMBER_ID_2);
			assertThat(notification.notificationType()).isEqualTo(NotificationType.RESERVATION_CREATED);
			assertThat(notification.content()).contains(member.getNickname(), item.getTitle(), "관심");
		}

		@Test
		@DisplayName("실패 - 이미 승인된 예약 존재")
		void fail_alreadyApproved() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, createGroup(), member);
			ReservationCreateData data = new ReservationCreateData(TEST_ITEM_ID, TEST_MEMBER_ID);

			given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
			given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
			given(reservationRepository.createIfNotApproved(TEST_ITEM_ID, TEST_MEMBER_ID)).willReturn(0);

			// when & then
			assertThatThrownBy(() -> reservationService.createReservation(data)).isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.RESERVATION_ALREADY_EXISTS);
			verify(notificationService, never()).createNotification(any());
		}

	}

	@Nested
	@DisplayName("예약 승인")
	class ApproveReservation {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, buyer);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID))
				.willReturn(Optional.of(reservation));
			given(reservationRepository.findByItemIdForUpdate(TEST_ITEM_ID)).willReturn(List.of(reservation));

			// when
			reservationService.approveReservation(TEST_MEMBER_ID, TEST_RESERVATION_ID);

			// then
			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
			verify(notificationService).createNotification(any());
		}

		@Test
		@DisplayName("실패 - 권한 없음")
		void fail_forbidden() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, buyer);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID))
				.willReturn(Optional.of(reservation));

			// when & then - buyer(ID=2) tries to approve
			assertThatThrownBy(() -> reservationService.approveReservation(TEST_MEMBER_ID_2, TEST_RESERVATION_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		}

		@Test
		@DisplayName("실패 - 이미 승인된 예약 존재")
		void fail_alreadyApprovedExists() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Member otherBuyer = createMember(3L, "other@test.com");
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation target = createReservation(TEST_RESERVATION_ID, item, buyer);
			Reservation approved = createReservation(2L, item, otherBuyer, ReservationStatus.APPROVED);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID)).willReturn(Optional.of(target));
			given(reservationRepository.findByItemIdForUpdate(TEST_ITEM_ID)).willReturn(List.of(approved, target));

			// when & then
			assertThatThrownBy(() -> reservationService.approveReservation(TEST_MEMBER_ID, TEST_RESERVATION_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.RESERVATION_ALREADY_EXISTS);
		}

		@Test
		@DisplayName("실패 - 잠금 대상 예약 없음")
		void fail_targetNotFoundInLockedRows() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, buyer);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID))
				.willReturn(Optional.of(reservation));
			given(reservationRepository.findByItemIdForUpdate(TEST_ITEM_ID)).willReturn(List.of());

			// when & then
			assertThatThrownBy(() -> reservationService.approveReservation(TEST_MEMBER_ID, TEST_RESERVATION_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.RESERVATION_NOT_FOUND);
		}

	}

	@Nested
	@DisplayName("거래 완료")
	class CompleteReservation {

		@Test
		@DisplayName("성공 - 판매자")
		void success_owner() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, buyer, ReservationStatus.APPROVED);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID))
				.willReturn(Optional.of(reservation));

			// when
			reservationService.completeReservation(TEST_MEMBER_ID, TEST_RESERVATION_ID);

			// then
			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
			verify(notificationService).createNotification(any());
		}

		@Test
		@DisplayName("성공 - 구매자")
		void success_buyer() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, buyer, ReservationStatus.APPROVED);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID))
				.willReturn(Optional.of(reservation));

			// when
			reservationService.completeReservation(TEST_MEMBER_ID_2, TEST_RESERVATION_ID);

			// then
			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
			verify(notificationService).createNotification(any());
		}

	}

	@Nested
	@DisplayName("예약 취소")
	class CancelReservation {

		@Test
		@DisplayName("성공")
		void success() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Member buyer = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, buyer);

			given(reservationRepository.findByIdWithItemAndMember(TEST_RESERVATION_ID))
				.willReturn(Optional.of(reservation));

			// when
			reservationService.cancelReservation(TEST_MEMBER_ID, TEST_RESERVATION_ID);

			// then
			assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
			verify(notificationService).createNotification(any());
		}

	}

	@Nested
	@DisplayName("예약 목록 조회")
	class GetReservations {

		@Test
		@DisplayName("상품별 예약 조회")
		void getReservationsByItem() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, createGroup(), member);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, member);

			given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));
			given(reservationRepository.findByItemId(TEST_ITEM_ID)).willReturn(List.of(reservation));

			// when
			List<ReservationResult> results = reservationService.getReservationsByItem(TEST_MEMBER_ID, TEST_ITEM_ID);

			// then
			assertThat(results).hasSize(1);
		}

		@Test
		@DisplayName("상품별 예약 조회 실패 - 권한 없음")
		void getReservationsByItem_fail_forbidden() {
			// given
			Member owner = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, createGroup(), owner);
			given(itemRepository.findById(TEST_ITEM_ID)).willReturn(Optional.of(item));

			// when & then
			assertThatThrownBy(() -> reservationService.getReservationsByItem(TEST_MEMBER_ID_2, TEST_ITEM_ID))
				.isInstanceOf(CoreException.class)
				.hasFieldOrPropertyWithValue("errorType", ErrorType.FORBIDDEN);
		}

		@Test
		@DisplayName("내 예약 조회")
		void getMyReservations() {
			// given
			Member member = createMember(TEST_MEMBER_ID);
			Item item = createItem(TEST_ITEM_ID, createGroup(), member);
			Reservation reservation = createReservation(TEST_RESERVATION_ID, item, member);

			given(reservationRepository.findByMemberId(TEST_MEMBER_ID)).willReturn(List.of(reservation));

			// when
			List<ReservationResult> results = reservationService.getMyReservations(TEST_MEMBER_ID);

			// then
			assertThat(results).hasSize(1);
		}

	}

}
