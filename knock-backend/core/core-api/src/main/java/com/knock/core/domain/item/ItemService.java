package com.knock.core.domain.item;

import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ReservationStatus;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemListQuery;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.reservation.Reservation;
import com.knock.storage.db.core.reservation.ReservationRepository;
import com.knock.storage.db.core.seller.SellerAccessMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

	private static final Duration VIEW_COUNT_DEDUP_TTL = Duration.ofMinutes(30);

	private static final String VIEW_COUNT_DEDUP_KEY_PREFIX = "item:view:dedup:";

	private final ItemRepository itemRepository;

	private final MemberRepository memberRepository;

	private final ReservationRepository reservationRepository;

	private final SellerAccessMemberRepository sellerAccessMemberRepository;

	private final RedisTemplate<String, Object> redisTemplate;

	@Transactional
	public ItemCreateResult createItem(Long memberId, ItemCreateData data) {
		validateTradeLocation(data);
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));

		Item item = Item.create(member, data.title(), data.description(), data.price(), data.type());
		item.updateTradeLocation(normalizeText(data.tradeLocationName()), normalizeText(data.tradeLocationAddress()),
				data.tradeLatitude(), data.tradeLongitude());

		Item savedItem = itemRepository.save(item, data.imageUrls());
		return new ItemCreateResult(savedItem.getId(), savedItem.getPublicId());
	}

	@Transactional(readOnly = true)
	public ItemReadResult getItem(Long itemId) {
		Item item = itemRepository.findByIdWithImages(itemId)
			.orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		return ItemReadResult.from(item, item.getImages());
	}

	@Transactional(readOnly = true)
	public ItemReadResult getItemForOwner(Long memberId, Long itemId) {
		Item item = itemRepository.findByIdWithImages(itemId)
			.orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));
		validateItemOwner(memberId, item);
		return ItemReadResult.from(item, item.getImages());
	}

	@Transactional(readOnly = true)
	public ItemReadResult getItemByPublicId(Long viewerMemberId, String publicId) {
		Item item = itemRepository.findByPublicIdWithImages(publicId)
			.orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));
		validateSellerAccess(viewerMemberId, item.getMember().getId());
		return ItemReadResult.from(item, item.getImages());
	}

	@Transactional(readOnly = true)
	public List<ItemListResult> getMySellingItems(Long memberId) {
		return itemRepository.findOwnerInventoryWithLikes(memberId).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<ItemListResult> getMarketplaceItems(Long viewerMemberId, ItemListQuery query) {
		validateAuthenticated(viewerMemberId);
		return itemRepository.findAccessibleListingsWithLikes(viewerMemberId, query).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<ItemListResult> getSellingItemsByMember(Long viewerMemberId, Long memberId, ItemListQuery query) {
		memberRepository.findById(memberId).orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		validateSellerAccess(viewerMemberId, memberId);
		return itemRepository.findPublicListingsByMemberIdWithLikes(memberId, query).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
	}

	@Transactional
	public void deleteItem(Long memberId, Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		validateItemOwner(memberId, item);
		cancelActiveReservations(itemId);
		itemRepository.delete(item);
	}

	private void validateItemOwner(Long memberId, Item item) {
		if (!item.getMember().getId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
	}

	private void validateSellerAccess(Long viewerMemberId, Long sellerId) {
		validateAuthenticated(viewerMemberId);
		if (sellerId.equals(viewerMemberId)) {
			return;
		}
		if (!sellerAccessMemberRepository.existsActiveBySellerIdAndMemberId(sellerId, viewerMemberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
	}

	private void validateAuthenticated(Long memberId) {
		if (memberId == null) {
			throw new CoreException(ErrorType.AUTHENTICATION_FAILED);
		}
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
		if (isBlank(data.tradeLocationName()) || isBlank(data.tradeLocationAddress())) {
			throw new CoreException(ErrorType.VALIDATION_ERROR);
		}

		boolean hasLatitude = data.tradeLatitude() != null;
		boolean hasLongitude = data.tradeLongitude() != null;
		if (!hasLatitude || !hasLongitude) {
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
		if (isBlank(value)) {
			return null;
		}
		return value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	@Async
	@Transactional
	public void increaseViewCount(Long itemId, Long writerId, Long viewerMemberId, String viewerSessionId) {
		if (itemId == null || isWriterView(writerId, viewerMemberId)) {
			return;
		}
		String viewerKey = resolveViewerKey(viewerMemberId, viewerSessionId);
		if (viewerKey == null || !markFirstView(itemId, viewerKey)) {
			return;
		}
		itemRepository.increaseViewCountById(itemId);
	}

	private boolean isWriterView(Long writerId, Long viewerMemberId) {
		return writerId != null && writerId.equals(viewerMemberId);
	}

	private String resolveViewerKey(Long viewerMemberId, String viewerSessionId) {
		if (viewerMemberId != null) {
			return "member:" + viewerMemberId;
		}
		if (viewerSessionId == null || viewerSessionId.isBlank()) {
			return null;
		}
		return "session:" + viewerSessionId;
	}

	private boolean markFirstView(Long itemId, String viewerKey) {
		try {
			String key = VIEW_COUNT_DEDUP_KEY_PREFIX + itemId + ":" + viewerKey;
			return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", VIEW_COUNT_DEDUP_TTL));
		}
		catch (RuntimeException e) {
			return false;
		}
	}

}
