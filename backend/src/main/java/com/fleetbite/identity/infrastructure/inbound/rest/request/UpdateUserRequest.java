package com.fleetbite.identity.infrastructure.inbound.rest.request;

import com.fleetbite.identity.domain.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		@NotBlank @Size(max = 120) String fullName,
		@NotNull UserRole role) {
}
