package com.fleetbite.identity.infrastructure.inbound.rest.request;

import com.fleetbite.identity.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotBlank @Size(max = 120) String fullName,
		@NotNull UserRole role) {
}
