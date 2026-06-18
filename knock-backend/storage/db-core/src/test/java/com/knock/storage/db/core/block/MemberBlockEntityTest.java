package com.knock.storage.db.core.block;

import com.knock.core.enums.BlockStatus;
import com.knock.storage.db.core.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberBlockEntityTest {

	@Test
	@DisplayName("회원 차단 생성 시 차단자와 대상, 활성 상태를 저장한다")
	void createInitializesMemberBlock() {
		Member blocker = Member.create("blocker@test.com", "Blocker", "password", "blocker", "LOCAL");
		Member blocked = Member.create("blocked@test.com", "Blocked", "password", "blocked", "LOCAL");

		MemberBlock block = MemberBlock.create(blocker, blocked);

		assertThat(block.getBlocker()).isSameAs(blocker);
		assertThat(block.getBlocked()).isSameAs(blocked);
		assertThat(block.getStatus()).isEqualTo(BlockStatus.ACTIVE);
	}

}
