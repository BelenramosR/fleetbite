package com.fleetbite.identity.infrastructure.outbound.persistence;

import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "email", expression = "java(user.email())")
	@Mapping(target = "passwordHash", expression = "java(user.passwordHash())")
	@Mapping(target = "fullName", expression = "java(user.fullName())")
	@Mapping(target = "role", expression = "java(user.role())")
	@Mapping(target = "status", expression = "java(user.status())")
	@Mapping(target = "createdAt", expression = "java(user.createdAt())")
	@Mapping(target = "updatedAt", expression = "java(user.updatedAt())")
	protected abstract void copyPersistableState(User user, @MappingTarget UserJpaEntity entity);

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
