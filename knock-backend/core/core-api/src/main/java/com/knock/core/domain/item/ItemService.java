package com.knock.core.domain.item;

import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemImage;
import com.knock.storage.db.core.item.ItemImageRepository;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

	private final ItemRepository itemRepository;
	private final MemberRepository memberRepository;
	private final GroupRepository groupRepository;
	private final ItemImageRepository itemImageRepository;

	private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

	@Transactional
	public ItemCreateResult createItem(Long memberId, Long groupId, ItemCreateData data) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		Group group = groupRepository.findGroupByGroupId(groupId)
				.orElseThrow(() -> new CoreException(ErrorType.GROUP_NOT_FOUND));

		Item item = Item.create(group, member, data.title(), data.description(), data.price(), data.type(),
				data.category());

		Item savedItem = itemRepository.save(item, data.imageUrls());
		return new ItemCreateResult(savedItem.getId());
	}

	public ItemReadResult getItem(Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new CoreException(ErrorType.ITEM_NOT_FOUND));

		List<ItemImage> images = itemImageRepository.findByItem(item);
		return ItemReadResult.from(item, images);
	}

	public List<ItemListResult> getItemsByGroup(Long groupId) {
		List<Item> items = itemRepository.findByGroupId(groupId);

		return items.stream().map(item -> {
			List<ItemImage> images = item.getImages();
			String thumbnailUrl = images.isEmpty() ? null : images.get(0).getImageUrl();
			return ItemListResult.from(item, thumbnailUrl);
		}).toList();
	}

	public List<ItemListResult> getMySellingItems(Long memberId) {
		List<Item> items = itemRepository.findByMemberId(memberId);

		return items.stream().map(item -> {
			List<ItemImage> images = item.getImages();
			String thumbnailUrl = images.isEmpty() ? null : images.get(0).getImageUrl();
			return ItemListResult.from(item, thumbnailUrl);
		}).toList();
	}

	public void increaseViewCount(Long itemId, Long memberId) {
		String logKey = "item:view:log:" + itemId + ":" + memberId;
		String countKey = "item:viewCount:" + itemId;

		Boolean hasViewed = redisTemplate.hasKey(logKey);

		if (!hasViewed) {
			redisTemplate.opsForValue().increment(countKey);
			redisTemplate.opsForValue().set(logKey, "1", java.time.Duration.ofMinutes(30));
		}
	}

}
