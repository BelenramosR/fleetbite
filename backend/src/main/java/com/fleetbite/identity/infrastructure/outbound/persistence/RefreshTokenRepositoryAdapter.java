package com.fleetbite.identity.infrastructure.outbound.persistence;

import com.fleetbite.identity.application.port.out.RefreshTokenRepositoryPort;
import com.fleetbite.identity.domain.model.RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

	private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;
	private final RefreshTokenPersistenceMapper refreshTokenPersistenceMapper;

	public RefreshTokenRepositoryAdapter(
			SpringDataRefreshTokenRepository springDataRefreshTokenRepository,
			RefreshTokenPersistenceMapper refreshTokenPersistenceMapper) {
		this.springDataRefreshTokenRepository = Objects.requireNonNull(springDataRefreshTokenRepository);
		this.refreshTokenPersistenceMapper = Objects.requireNonNull(refreshTokenPersistenceMapper);
	}

	@Override
	public RefreshToken save(RefreshToken refreshToken) {
		Objects.requireNonNull(refreshToken, "refreshToken is required");
		RefreshTokenJpaEntity entity = refreshTokenPersistenceMapper.toEntity(refreshToken);
		RefreshTokenJpaEntity saved = springDataRefreshTokenRepository.save(entity);
		return refreshTokenPersistenceMapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RefreshToken> findByHash(String tokenHash) {
		Objects.requireNonNull(tokenHash, "tokenHash is required");
		return springDataRefreshTokenRepository.findByTokenHash(tokenHash)
				.map(refreshTokenPersistenceMapper::toDomain);
	}

	@Override
	@Transactional
	public void revoke(UUID id, OffsetDateTime revokedAt) {
		Objects.requireNonNull(id, "id is required");
		Objects.requireNonNull(revokedAt, "revokedAt is required");
		springDataRefreshTokenRepository.revokeById(id, revokedAt);
	}

	@Override
	@Transactional
	public int deleteExpired(OffsetDateTime now) {
		Objects.requireNonNull(now, "now is required");
		return springDataRefreshTokenRepository.deleteExpired(now);
	}
}
