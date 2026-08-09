package com.fleetbite.identity.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Opaque refresh token")
public record RefreshTokenRequest(
		@Schema(description = "Refresh token issued at login or previous refresh",
				example = "550e8400-e29b-41d4-a716-446655440000",
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String refreshToken) {
}
