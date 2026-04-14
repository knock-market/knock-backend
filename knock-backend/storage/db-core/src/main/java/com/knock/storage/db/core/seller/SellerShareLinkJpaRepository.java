package com.knock.storage.db.core.seller;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerShareLinkJpaRepository extends JpaRepository<SellerShareLink, Long> {

	Optional<SellerShareLink> findByToken(String token);

	boolean existsByToken(String token);

}
