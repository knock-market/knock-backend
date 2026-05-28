package com.knock.storage.db.core.seller;

import java.util.List;
import java.util.Optional;

public interface SellerShareLinkRepository {

	SellerShareLink save(SellerShareLink sellerShareLink);

	Optional<SellerShareLink> findByToken(String token);

	Optional<SellerShareLink> findByTokenForUpdate(String token);

	List<SellerShareLink> findAllByMemberId(Long memberId);

	Optional<SellerShareLink> findLatestByMemberId(Long memberId);

	boolean existsByToken(String token);

}
