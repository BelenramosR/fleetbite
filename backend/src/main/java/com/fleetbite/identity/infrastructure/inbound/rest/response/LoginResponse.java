package com.fleetbite.identity.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token and opaque refresh token")
public record LoginResponse(
		@Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
		String accessToken,
		@Schema(description = "Token type", example = "Bearer")
		String tokenType,
		@Schema(description = "Seconds until access token expiration", example = "3600")
		long expiresIn,
		@Schema(description = "Opaque refresh token (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
		String refreshToken) {
}
