package com.fleetbite.shared.infrastructure.inbound.rest;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error body returned by the GlobalExceptionHandler and security handlers")
public record ApiErrorResponse(
		@Schema(description = "Error timestamp (UTC instant)", example = "2026-08-09T06:00:00Z")
		Instant timestamp,
		@Schema(description = "HTTP status code", example = "409")
		int status,
		@Schema(description = "Stable application error code", example = "INVALID_ORDER_TRANSITION")
		String code,
		@Schema(description = "Human-readable message", example = "The order cannot transition from DELIVERED to PREPARING")
		String message,
		@Schema(description = "Request path", example = "/api/v1/orders/11111111-1111-1111-1111-111111111111/confirm")
		String path) {
}
