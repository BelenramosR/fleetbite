package com.fleetbite.shared.infrastructure.inbound.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single error entry inside ApiResponse.errors")
public record ApiErrorItem(
		@Schema(description = "Request field related to the error, when applicable", example = "email", nullable = true)
		String field,
		@Schema(description = "Human-readable error message", example = "email is required")
		String message) {

	public ApiErrorItem(String message) {
		this(null, message);
	}
}
