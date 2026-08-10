package com.fleetbite.identity.application.dto;

import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.identity.domain.model.UserStatus;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record UserResult(UUID id, String email, String fullName, UserRole role, UserStatus status,
		OffsetDateTime createdAt, OffsetDateTime updatedAt) {
	public static UserResult from(User user) {
		Objects.requireNonNull(user, "user is required");
		return new UserResult(user.id(), user.email(), user.fullName(), user.role(), user.status(),
				user.createdAt(), user.updatedAt());
	}
}
