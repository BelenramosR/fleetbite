package com.fleetbite.order.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Append-only order history event")
public record OrderHistoryResponse(
		UUID id,
		@Schema(example = "CONFIRMED") String eventType,
		@Schema(example = "CREATED") String previousStatus,
		@Schema(example = "CONFIRMED") String newStatus,
		String description,
		@Schema(example = "2026-08-10T18:35:00-05:00") OffsetDateTime createdAt) {
}
