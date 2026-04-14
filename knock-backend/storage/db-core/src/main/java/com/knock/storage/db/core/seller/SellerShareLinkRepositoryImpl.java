package com.knock.storage.db.core.seller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
	public boolean existsByToken(String token) {
		return sellerShareLinkJpaRepository.existsByToken(token);
	}

}
