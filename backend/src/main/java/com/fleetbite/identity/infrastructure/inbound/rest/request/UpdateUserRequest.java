package com.fleetbite.identity.infrastructure.inbound.rest.request;

import com.fleetbite.identity.domain.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Update user profile. Does not change password.")
public record UpdateUserRequest(
		@Schema(example = "FleetBite Operator", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 120) String fullName,
		@Schema(implementation = UserRole.class, requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull UserRole role) {
}
