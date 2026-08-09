package com.fleetbite.order.infrastructure.inbound.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Order resource. Timestamps use offset -05:00 (America/Lima business clock).")
public record OrderResponse(
		UUID id,
		@Schema(example = "ORD-2026-0001") String code,
		String customerName,
		String customerPhone,
		String deliveryAddress,
		double deliveryLatitude,
		double deliveryLongitude,
		BigDecimal totalAmount,
		@Schema(allowableValues = {"NORMAL", "HIGH"}, example = "NORMAL") String priority,
		@Schema(example = "CREATED") String status,
		@Schema(example = "2026-08-10T19:15:00-05:00") OffsetDateTime promisedDeliveryAt,
		@Schema(example = "2026-08-10T18:30:00-05:00") OffsetDateTime createdAt,
		OffsetDateTime confirmedAt,
		OffsetDateTime preparationStartedAt,
		OffsetDateTime readyAt,
		OffsetDateTime assignedAt,
		OffsetDateTime pickedUpAt,
		OffsetDateTime inTransitAt,
		OffsetDateTime deliveredAt,
		OffsetDateTime cancelledAt,
		OffsetDateTime failedDeliveryAt) {
}
