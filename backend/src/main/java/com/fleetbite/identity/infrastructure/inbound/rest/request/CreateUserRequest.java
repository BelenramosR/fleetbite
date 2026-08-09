package com.fleetbite.identity.infrastructure.inbound.rest.request;

import com.fleetbite.identity.domain.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Create user request. Password is write-only and never returned.")
public record CreateUserRequest(
		@Schema(example = "operator@fleetbite.local", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Email @Size(max = 255) String email,
		@Schema(example = "Fleetbite1!", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(min = 8, max = 100) String password,
		@Schema(example = "FleetBite Operator", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 120) String fullName,
		@Schema(implementation = UserRole.class, requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull UserRole role) {
}
