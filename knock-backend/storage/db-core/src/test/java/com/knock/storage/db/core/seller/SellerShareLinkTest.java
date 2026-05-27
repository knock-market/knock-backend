package com.knock.storage.db.core.seller;

import com.knock.storage.db.core.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SellerShareLinkTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 27, 12, 0);

	@Test
	@DisplayName("공유 링크 생성 시 기본 상태는 활성이고 카운터는 0이다")
	void create_initializesActiveLink() {
		// given
		Member member = createMember();
		LocalDateTime expiresAt = NOW.plusHours(1);

		// when
		SellerShareLink shareLink = SellerShareLink.create(member, "share-token", expiresAt);

		// then
		assertThat(shareLink.getMember()).isEqualTo(member);
		assertThat(shareLink.getToken()).isEqualTo("share-token");
		assertThat(shareLink.getExpiresAt()).isEqualTo(expiresAt);
		assertThat(shareLink.isActive()).isTrue();
		assertThat(shareLink.getClickCount()).isZero();
		assertThat(shareLink.getUseCount()).isZero();
	}

	@Test
	@DisplayName("공유 링크는 만료 시간과 활성 상태로 사용 가능 여부를 판단한다")
	void isAvailable_usesActiveStateAndExpiration() {
		// given
		SellerShareLink expiringLink = SellerShareLink.create(createMember(), "expiring-token", NOW.plusMinutes(10));
		SellerShareLink permanentLink = SellerShareLink.create(createMember(), "permanent-token", null);

		// when & then
		assertThat(expiringLink.isExpired(NOW)).isFalse();
		assertThat(expiringLink.isAvailable(NOW)).isTrue();
		assertThat(expiringLink.isExpired(NOW.plusMinutes(11))).isTrue();
		assertThat(expiringLink.isAvailable(NOW.plusMinutes(11))).isFalse();
		assertThat(permanentLink.isExpired(NOW.plusYears(1))).isFalse();
		assertThat(permanentLink.isAvailable(NOW.plusYears(1))).isTrue();
	}

	@Test
	@DisplayName("공유 링크 클릭과 사용 카운터를 기록한다")
	void recordStats_incrementsCounters() {
		// given
		SellerShareLink shareLink = SellerShareLink.create(createMember(), "stats-token", NOW.plusHours(1));

		// when
		shareLink.recordClick();
		shareLink.recordClick();
		shareLink.recordUse();

		// then
		assertThat(shareLink.getClickCount()).isEqualTo(2L);
		assertThat(shareLink.getUseCount()).isEqualTo(1L);
	}

	@Test
	@DisplayName("공유 링크를 비활성화하면 사용 가능하지 않다")
	void deactivate_marksLinkUnavailable() {
		// given
		SellerShareLink shareLink = SellerShareLink.create(createMember(), "inactive-token", NOW.plusHours(1));

		// when
		shareLink.deactivate();

		// then
		assertThat(shareLink.isActive()).isFalse();
		assertThat(shareLink.isAvailable(NOW)).isFalse();
	}

	private Member createMember() {
		return Member.create("seller-share-entity@test.com", "Name", "Pass", "Nick", "LOCAL");
	}

}
