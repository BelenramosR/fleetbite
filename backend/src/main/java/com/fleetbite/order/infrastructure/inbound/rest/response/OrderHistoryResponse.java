package com.fleetbite.order.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderHistoryResponse(
		UUID id,
		String eventType,
		String previousStatus,
		String newStatus,
		String description,
		OffsetDateTime createdAt) {
}
