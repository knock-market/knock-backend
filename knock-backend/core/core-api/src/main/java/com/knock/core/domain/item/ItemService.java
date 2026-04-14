package com.knock.core.domain.item;

import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ReservationStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	private final MemberRepository memberRepository;

	private final GroupRepository groupRepository;

	private final ReservationRepository reservationRepository;

	@Transactional
	public ItemCreateResult createItem(Long memberId, Long groupId, ItemCreateData data) {
		validateTradeLocation(data);
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		Group group = groupRepository.findGroupByGroupId(groupId)
			.orElseThrow(() -> new CoreException(ErrorType.GROUP_NOT_FOUND));

		Item item = Item.create(group, member, data.title(), data.description(), data.price(), data.type(),
				data.category());
		item.updateTradeLocation(normalizeText(data.tradeLocationName()), normalizeText(data.tradeLocationAddress()),
				data.tradeLatitude(), data.tradeLongitude());

		Item savedItem = itemRepository.save(item, data.imageUrls());
		return new ItemCreateResult(savedItem.getId());
	}

	@Transactional(readOnly = true)
	public ItemReadResult getItem(Long itemId) {
		Item item = itemRepository.findByIdWithImages(itemId)
			.orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		return ItemReadResult.from(item, item.getImages());
	}

	@Transactional(readOnly = true)
	public List<ItemListResult> getItemsByGroup(Long groupId) {
		return itemRepository.findByGroupIdWithLikes(groupId).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<ItemListResult> getMySellingItems(Long memberId) {
		return itemRepository.findByMemberIdWithLikes(memberId).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<ItemListResult> getSellingItemsByMember(Long memberId) {
		memberRepository.findById(memberId).orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		return itemRepository.findByMemberIdWithLikes(memberId).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
	}

	@Transactional
	public void deleteItem(Long memberId, Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		cancelActiveReservations(itemId);
		itemRepository.delete(item);
	}

	private void cancelActiveReservations(Long itemId) {
		reservationRepository.findByItemId(itemId)
			.stream()
			.filter(this::isActiveReservation)
			.forEach(Reservation::cancel);
	}

	private boolean isActiveReservation(Reservation reservation) {
		return reservation.getStatus() == ReservationStatus.WAITING
				|| reservation.getStatus() == ReservationStatus.APPROVED;
	}

	private void validateTradeLocation(ItemCreateData data) {
		boolean hasLatitude = data.tradeLatitude() != null;
		boolean hasLongitude = data.tradeLongitude() != null;
		if (hasLatitude != hasLongitude) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
		if (hasLatitude && (data.tradeLatitude() < -90 || data.tradeLatitude() > 90)) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
		if (hasLongitude && (data.tradeLongitude() < -180 || data.tradeLongitude() > 180)) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}
	}

	private String normalizeText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	// todo : 로직 완성 필요
	@Async
	@Transactional
	public void increaseViewCount(Long itemId, Long memberId) {
		// String logKey = "item:view:log:" + itemId + ":" + memberId;
		// String countKey = "item:viewCount:" + itemId;
		//
		// Boolean hasViewed = redisTemplate.hasKey(logKey);
		//
		// if (!hasViewed) {
		// redisTemplate.opsForValue().increment(countKey);
		// redisTemplate.opsForValue().set(logKey, "1",
		// java.time.Duration.ofMinutes(30));
		// }
		itemRepository.increaseViewCountById(itemId);
	}

}
