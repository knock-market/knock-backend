package com.knock.core.domain.block;

import com.knock.core.domain.block.dto.BlockResult;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.block.MemberBlock;
import com.knock.storage.db.core.block.MemberBlockRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.knock.core.support.TestConstants.*;
import static com.knock.core.support.TestFixtures.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

	@InjectMocks
	private BlockService blockService;

	@Mock
	private MemberBlockRepository memberBlockRepository;

	@Mock
	private MemberRepository memberRepository;

	@Test
	@DisplayName("회원 차단 성공")
	void blockMember_success() {
		Member blocker = createMember(TEST_MEMBER_ID);
		Member blocked = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(blocker));
		given(memberRepository.findById(TEST_MEMBER_ID_2)).willReturn(Optional.of(blocked));
		given(memberBlockRepository.findByBlockerAndBlockedWithDeleted(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.willReturn(Optional.empty());
		given(memberBlockRepository.save(any(MemberBlock.class))).willAnswer(invocation -> {
			MemberBlock block = invocation.getArgument(0);
			ReflectionTestUtils.setField(block, "id", 10L);
			return block;
		});

		BlockResult result = blockService.blockMember(TEST_MEMBER_ID, TEST_MEMBER_ID_2);

		assertThat(result.memberId()).isEqualTo(TEST_MEMBER_ID_2);
		verify(memberBlockRepository).save(any(MemberBlock.class));
	}

	@Test
	@DisplayName("반복 차단은 기존 차단을 반환한다")
	void blockMember_idempotent() {
		Member blocker = createMember(TEST_MEMBER_ID);
		Member blocked = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
		MemberBlock existing = MemberBlock.create(blocker, blocked);
		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(blocker));
		given(memberRepository.findById(TEST_MEMBER_ID_2)).willReturn(Optional.of(blocked));
		given(memberBlockRepository.findByBlockerAndBlockedWithDeleted(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.willReturn(Optional.of(existing));

		BlockResult result = blockService.blockMember(TEST_MEMBER_ID, TEST_MEMBER_ID_2);

		assertThat(result.memberId()).isEqualTo(TEST_MEMBER_ID_2);
	}

	@Test
	@DisplayName("삭제된 차단은 복구된다")
	void blockMember_restoreDeleted() {
		Member blocker = createMember(TEST_MEMBER_ID);
		Member blocked = createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2);
		MemberBlock existing = MemberBlock.create(blocker, blocked);
		ReflectionTestUtils.setField(existing, "deletedAt", LocalDateTime.now());
		given(memberRepository.findById(TEST_MEMBER_ID)).willReturn(Optional.of(blocker));
		given(memberRepository.findById(TEST_MEMBER_ID_2)).willReturn(Optional.of(blocked));
		given(memberBlockRepository.findByBlockerAndBlockedWithDeleted(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.willReturn(Optional.of(existing));

		blockService.blockMember(TEST_MEMBER_ID, TEST_MEMBER_ID_2);

		assertThat(existing.getDeletedAt()).isNull();
	}

	@Test
	@DisplayName("자기 자신 차단은 실패한다")
	void blockMember_failSelfBlock() {
		assertThatThrownBy(() -> blockService.blockMember(TEST_MEMBER_ID, TEST_MEMBER_ID))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.SELF_BLOCK_NOT_ALLOWED);
	}

	@Test
	@DisplayName("차단 해제는 소유자에게 멱등적으로 동작한다")
	void unblockMember_success() {
		MemberBlock existing = MemberBlock.create(createMember(TEST_MEMBER_ID),
				createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2));
		given(memberBlockRepository.findActiveByBlockerAndBlocked(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.willReturn(Optional.of(existing));

		blockService.unblockMember(TEST_MEMBER_ID, TEST_MEMBER_ID_2);

		verify(memberBlockRepository).delete(existing);
	}

	@Test
	@DisplayName("내 차단 목록 조회")
	void getMyBlocks_success() {
		MemberBlock block = MemberBlock.create(createMember(TEST_MEMBER_ID),
				createMember(TEST_MEMBER_ID_2, TEST_EMAIL_2));
		given(memberBlockRepository.findAllActiveByBlockerId(TEST_MEMBER_ID)).willReturn(List.of(block));

		List<BlockResult> results = blockService.getMyBlocks(TEST_MEMBER_ID);

		assertThat(results).hasSize(1);
		assertThat(results.getFirst().memberId()).isEqualTo(TEST_MEMBER_ID_2);
	}

	@Test
	@DisplayName("상호작용 차단 검증")
	void validateInteractionAllowed_failBlocked() {
		given(memberBlockRepository.existsActiveBetween(TEST_MEMBER_ID, TEST_MEMBER_ID_2)).willReturn(true);

		assertThatThrownBy(() -> blockService.validateInteractionAllowed(TEST_MEMBER_ID, TEST_MEMBER_ID_2))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.BLOCKED_INTERACTION);
	}

}
