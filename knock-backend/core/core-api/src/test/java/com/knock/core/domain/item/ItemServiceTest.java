package com.knock.core.domain.item;

import com.knock.core.domain.item.dto.ItemCreateData;
import com.knock.core.domain.item.dto.ItemCreateResult;
import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.item.dto.ItemReadResult;
import com.knock.core.enums.ItemCategory;
import com.knock.core.enums.ItemType;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.group.Group;
import com.knock.storage.db.core.group.GroupRepository;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Nested
    @DisplayName("상품 등록")
    class CreateItem {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Group group = createGroup(TEST_GROUP_ID, TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, group, member);
            ItemCreateData data = new ItemCreateData(
                    TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
                    ItemType.SELL, ItemCategory.DIGITAL_DEVICE, List.of(TEST_IMAGE_URL));

            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
            given(groupRepository.findGroupByGroupId(TEST_GROUP_ID)).willReturn(Optional.of(group));
            given(itemRepository.save(any(Item.class), anyList())).willReturn(item);

            // when
            ItemCreateResult result = itemService.createItem(TEST_MEMBER_ID, TEST_GROUP_ID, data);

            // then
            assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
        }

        @Test
        @DisplayName("실패 - 회원 없음")
        void fail_memberNotFound() {
            // given
            ItemCreateData data = new ItemCreateData(
                    TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
                    ItemType.SELL, ItemCategory.DIGITAL_DEVICE, List.of());
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> itemService.createItem(TEST_MEMBER_ID, TEST_GROUP_ID, data))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 그룹 없음")
        void fail_groupNotFound() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            ItemCreateData data = new ItemCreateData(
                    TEST_ITEM_TITLE, TEST_ITEM_DESCRIPTION, TEST_ITEM_PRICE,
                    ItemType.SELL, ItemCategory.DIGITAL_DEVICE, List.of());
            given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(member));
            given(groupRepository.findGroupByGroupId(TEST_GROUP_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> itemService.createItem(TEST_MEMBER_ID, TEST_GROUP_ID, data))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.GROUP_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("상품 조회")
    class GetItem {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Group group = createGroup(TEST_GROUP_ID, TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, group, member);

            given(itemRepository.findByIdWithImages(TEST_ITEM_ID)).willReturn(Optional.of(item));

            // when
            ItemReadResult result = itemService.getItem(TEST_ITEM_ID);

            // then
            assertThat(result.id()).isEqualTo(TEST_ITEM_ID);
            assertThat(result.title()).isEqualTo(TEST_ITEM_TITLE);
        }

        @Test
        @DisplayName("실패 - 상품 없음")
        void fail_itemNotFound() {
            // given
            given(itemRepository.findByIdWithImages(TEST_ITEM_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> itemService.getItem(TEST_ITEM_ID))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("그룹별 상품 목록 조회")
    class GetItemsByGroup {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Group group = createGroup(TEST_GROUP_ID, TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, group, member);

            given(itemRepository.findByGroupId(TEST_GROUP_ID)).willReturn(List.of(item));

            // when
            List<ItemListResult> results = itemService.getItemsByGroup(TEST_GROUP_ID);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).id()).isEqualTo(TEST_ITEM_ID);
        }
    }

    @Nested
    @DisplayName("내 판매 상품 조회")
    class GetMySellingItems {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Member member = createMember(TEST_MEMBER_ID);
            Group group = createGroup(TEST_GROUP_ID, TEST_MEMBER_ID);
            Item item = createItem(TEST_ITEM_ID, group, member);

            given(itemRepository.findByMemberId(TEST_MEMBER_ID)).willReturn(List.of(item));

            // when
            List<ItemListResult> results = itemService.getMySellingItems(TEST_MEMBER_ID);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).id()).isEqualTo(TEST_ITEM_ID);
        }
    }
}
