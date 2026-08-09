package com.fleetbite.identity.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT login result")
public record LoginResponse(
		@Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
		String accessToken,
		@Schema(description = "Token type", example = "Bearer")
		String tokenType,
		@Schema(description = "Seconds until expiration", example = "3600")
		long expiresIn) {
}
