package com.knock.core.domain.item;

import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static com.knock.core.support.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemViewCountServiceTest {

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

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Test
	@DisplayName("로그인 사용자는 30분 내 첫 조회만 집계")
	void memberFirstView() {
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.setIfAbsent("item:view:dedup:" + TEST_ITEM_ID + ":member:" + TEST_MEMBER_ID, "1",
				Duration.ofMinutes(30)))
			.willReturn(true);

		itemService.increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID_2, TEST_MEMBER_ID, null);

		verify(itemRepository).increaseViewCountById(TEST_ITEM_ID);
	}

	@Test
	@DisplayName("로그인 사용자의 중복 조회는 집계하지 않음")
	void memberDuplicateView() {
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.setIfAbsent("item:view:dedup:" + TEST_ITEM_ID + ":member:" + TEST_MEMBER_ID, "1",
				Duration.ofMinutes(30)))
			.willReturn(false);

		itemService.increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID_2, TEST_MEMBER_ID, null);

		verify(itemRepository, never()).increaseViewCountById(any());
	}

	@Test
	@DisplayName("비로그인 사용자는 세션 ID 기준으로 집계")
	void guestSessionFirstView() {
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.setIfAbsent("item:view:dedup:" + TEST_ITEM_ID + ":session:guest-session", "1",
				Duration.ofMinutes(30)))
			.willReturn(true);

		itemService.increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID, null, "guest-session");

		verify(itemRepository).increaseViewCountById(TEST_ITEM_ID);
	}

	@Test
	@DisplayName("판매자 본인 조회는 집계하지 않음")
	void writerViewIgnored() {
		itemService.increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID, TEST_MEMBER_ID, null);

		verify(redisTemplate, never()).opsForValue();
		verify(itemRepository, never()).increaseViewCountById(any());
	}

	@Test
	@DisplayName("Redis 오류가 발생하면 조회수 집계만 생략")
	void redisErrorIgnored() {
		given(redisTemplate.opsForValue()).willThrow(new IllegalStateException("redis unavailable"));

		itemService.increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID_2, TEST_MEMBER_ID, null);

		verify(itemRepository, never()).increaseViewCountById(any());
	}

	@Test
	@DisplayName("조회자 식별자가 없으면 집계하지 않음")
	void missingViewerKeyIgnored() {
		itemService.increaseViewCount(TEST_ITEM_ID, TEST_MEMBER_ID, null, null);

		verify(redisTemplate, never()).opsForValue();
		verify(itemRepository, never()).increaseViewCountById(any());
	}

}
