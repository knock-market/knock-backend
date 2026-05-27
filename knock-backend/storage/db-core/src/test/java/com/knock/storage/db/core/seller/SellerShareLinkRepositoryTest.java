package com.knock.storage.db.core.seller;

import com.knock.storage.db.CoreDbContextTest;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SellerShareLinkRepositoryTest extends CoreDbContextTest {

	@Autowired
	private SellerShareLinkRepository sellerShareLinkRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("판매자 공유 링크 저장 및 토큰 조회 성공")
	void saveAndFindByToken() {
		// given
		Member member = memberRepository.save(Member.create("seller-share@test.com", "Name", "Pass", "Nick", "LOCAL"));
		LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
		SellerShareLink shareLink = SellerShareLink.create(member, "seller-share-token", expiresAt);

		// when
		SellerShareLink savedShareLink = sellerShareLinkRepository.save(shareLink);
		Optional<SellerShareLink> foundShareLink = sellerShareLinkRepository.findByToken("seller-share-token");

		// then
		assertThat(foundShareLink).isPresent();
		assertThat(foundShareLink.get().getId()).isEqualTo(savedShareLink.getId());
		assertThat(foundShareLink.get().getMember().getId()).isEqualTo(member.getId());
		assertThat(sellerShareLinkRepository.existsByToken("seller-share-token")).isTrue();
		assertThat(foundShareLink.get().isExpired(expiresAt.minusSeconds(1))).isFalse();
		assertThat(foundShareLink.get().isExpired(expiresAt.plusSeconds(1))).isTrue();
		assertThat(sellerShareLinkRepository.findAllByMemberId(member.getId())).hasSize(1);
	}

	@Test
	@DisplayName("회원의 최신 공유 링크 1개 조회 성공")
	void findLatestByMemberId() {
		// given
		Member member = memberRepository
			.save(Member.create("seller-share-latest@test.com", "Name", "Pass", "Nick", "LOCAL"));
		SellerShareLink firstShareLink = sellerShareLinkRepository
			.save(SellerShareLink.create(member, "seller-share-first-token", LocalDateTime.now().plusHours(1)));
		SellerShareLink latestShareLink = sellerShareLinkRepository
			.save(SellerShareLink.create(member, "seller-share-latest-token", LocalDateTime.now().plusHours(2)));

		// when
		Optional<SellerShareLink> foundShareLink = sellerShareLinkRepository.findLatestByMemberId(member.getId());

		// then
		assertThat(foundShareLink).isPresent();
		assertThat(foundShareLink.get().getId()).isNotEqualTo(firstShareLink.getId());
		assertThat(foundShareLink.get().getId()).isEqualTo(latestShareLink.getId());
		assertThat(foundShareLink.get().getToken()).isEqualTo("seller-share-latest-token");
	}

}
