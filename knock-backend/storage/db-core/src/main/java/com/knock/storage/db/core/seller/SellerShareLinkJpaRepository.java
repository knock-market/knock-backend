package com.knock.storage.db.core.seller;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerShareLinkJpaRepository extends JpaRepository<SellerShareLink, Long> {

	Optional<SellerShareLink> findByToken(String token);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from SellerShareLink s where s.token = :token")
	Optional<SellerShareLink> findByTokenForUpdate(@Param("token") String token);

	List<SellerShareLink> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

	Optional<SellerShareLink> findFirstByMemberIdOrderByCreatedAtDescIdDesc(Long memberId);

	boolean existsByToken(String token);

}
