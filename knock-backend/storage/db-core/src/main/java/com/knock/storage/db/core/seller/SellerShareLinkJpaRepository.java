package com.knock.storage.db.core.seller;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerShareLinkJpaRepository extends JpaRepository<SellerShareLink, Long> {

	Optional<SellerShareLink> findByToken(String token);

	List<SellerShareLink> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

	Optional<SellerShareLink> findFirstByMemberIdOrderByCreatedAtDescIdDesc(Long memberId);

	boolean existsByToken(String token);

}
