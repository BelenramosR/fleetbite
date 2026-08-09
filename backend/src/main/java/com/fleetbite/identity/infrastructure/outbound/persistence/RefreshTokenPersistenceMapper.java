package com.fleetbite.identity.infrastructure.outbound.persistence;

import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;

import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class RefreshTokenPersistenceMapper {

	public RefreshTokenJpaEntity toEntity(RefreshToken refreshToken) {
		Objects.requireNonNull(refreshToken, "refreshToken is required");
		RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
		entity.setId(refreshToken.id());
		entity.setUserId(refreshToken.userId().value());
		entity.setTokenHash(refreshToken.tokenHash());
		entity.setExpiresAt(refreshToken.expiresAt());
		entity.setCreatedAt(refreshToken.createdAt());
		entity.setRevokedAt(refreshToken.revokedAt());
		return entity;
	}

	public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return RefreshToken.reconstitute(
				entity.getId(),
				UserId.of(entity.getUserId()),
				entity.getTokenHash(),
				toBusinessOffset(entity.getExpiresAt()),
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getRevokedAt()));
	}

	protected OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
