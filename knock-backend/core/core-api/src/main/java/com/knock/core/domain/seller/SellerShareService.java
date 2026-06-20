package com.knock.core.domain.seller;

import com.knock.core.domain.item.dto.ItemListResult;
import com.knock.core.domain.seller.dto.SellerShareLinkCreateResult;
import com.knock.core.domain.seller.dto.SellerShareLinkStatsResult;
import com.knock.core.domain.seller.dto.SellerShopResult;
import com.knock.core.enums.InviteDuration;
import com.knock.core.support.error.CoreException;
import com.knock.core.support.error.ErrorType;
import com.knock.storage.db.core.item.Item;
import com.knock.storage.db.core.item.ItemListQuery;
import com.knock.storage.db.core.item.ItemRepository;
import com.knock.storage.db.core.member.Member;
import com.knock.storage.db.core.member.MemberRepository;
import com.knock.storage.db.core.seller.SellerShareLink;
import com.knock.storage.db.core.seller.SellerShareLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerShareService {

	private static final int TOKEN_BYTE_LENGTH = 24;

	private final SellerShareLinkRepository sellerShareLinkRepository;

	private final MemberRepository memberRepository;

	private final ItemRepository itemRepository;

	private final Clock clock;

	private final SecureRandom secureRandom = new SecureRandom();

	@Transactional
	public SellerShareLinkCreateResult createShareLink(Long memberId, InviteDuration duration) {
		Member member = memberRepository.findByIdForUpdate(memberId)
			.orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		InviteDuration normalizedDuration = duration == null ? InviteDuration.ONE_DAY : duration;
		LocalDateTime expiresAt = normalizedDuration.getDuration() == null ? null
				: LocalDateTime.now(clock).plus(normalizedDuration.getDuration());
		sellerShareLinkRepository.findAllByMemberId(memberId).forEach(SellerShareLink::deactivate);
		SellerShareLink saved = sellerShareLinkRepository
			.save(SellerShareLink.create(member, generateUniqueToken(), expiresAt));
		return new SellerShareLinkCreateResult(saved.getToken(), saved.getExpiresAt(), saved.isActive(),
				saved.getClickCount(), saved.getUseCount());
	}

	@Transactional
	public SellerShopResult getSellerShop(String token) {
		return getSellerShop(token, ItemListQuery.defaultQuery());
	}

	@Transactional
	public SellerShopResult getSellerShop(String token, ItemListQuery query) {
		SellerShareLink shareLink = sellerShareLinkRepository.findByTokenForUpdate(token)
			.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
		shareLink.recordClick();
		if (!shareLink.isAvailable(LocalDateTime.now(clock))) {
			throw new CoreException(ErrorType.NOT_FOUND);
		}
		shareLink.recordUse();
		Member seller = shareLink.getMember();
		List<ItemListResult> items = itemRepository.findPublicListingsByMemberIdWithLikes(seller.getId(), query).stream().map(row -> {
			Item item = (Item) row[0];
			String thumbnailUrl = (String) row[1];
			long likesCount = (Long) row[2];
			return ItemListResult.from(item, thumbnailUrl, likesCount);
		}).toList();
		return SellerShopResult.from(seller, items);
	}

	@Transactional(readOnly = true)
	public List<SellerShareLinkStatsResult> getMyShareLinks(Long memberId) {
		memberRepository.findById(memberId).orElseThrow(() -> new CoreException(ErrorType.MEMBER_NOT_FOUND));
		return sellerShareLinkRepository.findLatestByMemberId(memberId)
			.stream()
			.map(SellerShareLinkStatsResult::from)
			.toList();
	}

	@Transactional
	public void deactivateShareLink(Long memberId, String token) {
		SellerShareLink shareLink = sellerShareLinkRepository.findByTokenForUpdate(token)
			.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
		if (!shareLink.getMember().getId().equals(memberId)) {
			throw new CoreException(ErrorType.FORBIDDEN);
		}
		shareLink.deactivate();
	}

	private String generateUniqueToken() {
		String token;
		do {
			byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
			secureRandom.nextBytes(bytes);
			token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		}
		while (sellerShareLinkRepository.existsByToken(token));
		return token;
	}

}
