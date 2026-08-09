package com.fleetbite.identity.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

	Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

	@Modifying(clearAutomatically = true)
	@Query("update RefreshTokenJpaEntity r set r.revokedAt = :revokedAt where r.id = :id and r.revokedAt is null")
	int revokeById(@Param("id") UUID id, @Param("revokedAt") OffsetDateTime revokedAt);

	@Modifying(clearAutomatically = true)
	@Query("delete from RefreshTokenJpaEntity r where r.expiresAt < :now")
	int deleteExpired(@Param("now") OffsetDateTime now);
}
