package com.fleetbite.order.infrastructure.inbound.rest.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderResponse(
		UUID id,
		String code,
		String customerName,
		String customerPhone,
		String deliveryAddress,
		double deliveryLatitude,
		double deliveryLongitude,
		BigDecimal totalAmount,
		String priority,
		String status,
		OffsetDateTime promisedDeliveryAt,
		OffsetDateTime createdAt,
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
