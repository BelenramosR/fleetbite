package com.fleetbite.identity.infrastructure.outbound.persistence;

import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;

import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class UserPersistenceMapper {

	public UserJpaEntity toEntity(User user) {
		Objects.requireNonNull(user, "user is required");
		UserJpaEntity entity = new UserJpaEntity();
		entity.setId(user.id().value());
		copyPersistableState(user, entity);
		return entity;
	}

	public void copyToEntity(User user, UserJpaEntity existingEntity) {
		Objects.requireNonNull(user, "user is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(user.id().value())) {
			throw new IllegalArgumentException("cannot copy user onto entity with a different id");
		}
		copyPersistableState(user, existingEntity);
	}

	protected void copyPersistableState(User user, UserJpaEntity entity) {
		entity.setEmail(user.email());
		entity.setPasswordHash(user.passwordHash());
		entity.setFullName(user.fullName());
		entity.setRole(user.role());
		entity.setStatus(user.status());
		entity.setCreatedAt(user.createdAt());
		entity.setUpdatedAt(user.updatedAt());
	}

	public User toDomain(UserJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return User.reconstitute(
				UserId.of(entity.getId()),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getFullName(),
				entity.getRole(),
				entity.getStatus(),
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getUpdatedAt()));
	}

	protected OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
