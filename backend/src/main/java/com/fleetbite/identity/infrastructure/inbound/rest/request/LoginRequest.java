package com.fleetbite.identity.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequest(
		@Schema(example = "dispatcher@fleetbite.local", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Email String email,
		@Schema(example = "Fleetbite1!", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String password) {
}
