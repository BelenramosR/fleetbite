package com.fleetbite.identity.infrastructure.outbound.persistence;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class UserPersistenceMapper {
	public UserJpaEntity toEntity(User user) {
		Objects.requireNonNull(user, "user is required");
		UserJpaEntity entity = new UserJpaEntity();
		entity.setId(user.id());
		copyToEntity(user, entity);
		return entity;
	}
	public void copyToEntity(User user, UserJpaEntity entity) {
		Objects.requireNonNull(user, "user is required");
		Objects.requireNonNull(entity, "entity is required");
		if (entity.getId() != null && !entity.getId().equals(user.id())) {
			throw new IllegalArgumentException("cannot copy user onto entity with a different id");
		}
		entity.setEmail(user.email()); entity.setPasswordHash(user.passwordHash());
		entity.setFullName(user.fullName()); entity.setRole(user.role()); entity.setStatus(user.status());
		entity.setCreatedAt(user.createdAt()); entity.setUpdatedAt(user.updatedAt());
	}
	public User toDomain(UserJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return User.reconstitute(entity.getId(), entity.getEmail(), entity.getPasswordHash(),
				entity.getFullName(), entity.getRole(), entity.getStatus(),
				businessTime(entity.getCreatedAt()), businessTime(entity.getUpdatedAt()));
	}
	private OffsetDateTime businessTime(OffsetDateTime value) {
		return value == null ? null : value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
