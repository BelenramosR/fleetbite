package com.fleetbite.identity.infrastructure.outbound.persistence;
import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class RefreshTokenPersistenceMapper {
	public RefreshTokenJpaEntity toEntity(RefreshToken token) {
		Objects.requireNonNull(token, "refreshToken is required");
		RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
		entity.setId(token.id()); entity.setUserId(token.userId()); entity.setTokenHash(token.tokenHash());
		entity.setExpiresAt(token.expiresAt()); entity.setCreatedAt(token.createdAt()); entity.setRevokedAt(token.revokedAt());
		return entity;
	}
	public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return RefreshToken.reconstitute(entity.getId(), entity.getUserId(), entity.getTokenHash(),
				businessTime(entity.getExpiresAt()), businessTime(entity.getCreatedAt()), businessTime(entity.getRevokedAt()));
	}
	private OffsetDateTime businessTime(OffsetDateTime value) {
		return value == null ? null : value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
