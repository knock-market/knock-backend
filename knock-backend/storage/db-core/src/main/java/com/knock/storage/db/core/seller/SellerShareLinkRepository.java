package com.knock.storage.db.core.seller;

import java.util.Optional;

public interface SellerShareLinkRepository {

	SellerShareLink save(SellerShareLink sellerShareLink);

	Optional<SellerShareLink> findByToken(String token);

	boolean existsByToken(String token);

}
