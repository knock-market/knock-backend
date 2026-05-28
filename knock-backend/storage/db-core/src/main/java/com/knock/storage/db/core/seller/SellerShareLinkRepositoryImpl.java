package com.knock.storage.db.core.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerShareLinkRepositoryImpl implements SellerShareLinkRepository {

	private final SellerShareLinkJpaRepository sellerShareLinkJpaRepository;

	@Override
	public SellerShareLink save(SellerShareLink sellerShareLink) {
		return sellerShareLinkJpaRepository.save(sellerShareLink);
	}

	@Override
	public Optional<SellerShareLink> findByToken(String token) {
		return sellerShareLinkJpaRepository.findByToken(token);
	}

	@Override
	public Optional<SellerShareLink> findByTokenForUpdate(String token) {
		return sellerShareLinkJpaRepository.findByTokenForUpdate(token);
	}

	@Override
	public List<SellerShareLink> findAllByMemberId(Long memberId) {
		return sellerShareLinkJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
	}

	@Override
	public Optional<SellerShareLink> findLatestByMemberId(Long memberId) {
		return sellerShareLinkJpaRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(memberId);
	}

	@Override
	public boolean existsByToken(String token) {
		return sellerShareLinkJpaRepository.existsByToken(token);
	}

}
