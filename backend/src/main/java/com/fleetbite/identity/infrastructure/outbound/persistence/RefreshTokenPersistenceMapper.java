package com.fleetbite.identity.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class RefreshTokenPersistenceMapper {

	public RefreshTokenJpaEntity toEntity(RefreshToken refreshToken) {
		Objects.requireNonNull(refreshToken, "refreshToken is required");
		RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
		entity.setId(refreshToken.id());
		copyPersistableState(refreshToken, entity);
		return entity;
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", expression = "java(refreshToken.userId())")
	@Mapping(target = "tokenHash", expression = "java(refreshToken.tokenHash())")
	@Mapping(target = "expiresAt", expression = "java(refreshToken.expiresAt())")
	@Mapping(target = "createdAt", expression = "java(refreshToken.createdAt())")
	@Mapping(target = "revokedAt", expression = "java(refreshToken.revokedAt())")
	protected abstract void copyPersistableState(RefreshToken refreshToken, @MappingTarget RefreshTokenJpaEntity entity);

	public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return RefreshToken.reconstitute(
				entity.getId(),
				entity.getUserId(),
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
