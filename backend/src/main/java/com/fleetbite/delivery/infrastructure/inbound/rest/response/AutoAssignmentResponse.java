package com.fleetbite.delivery.infrastructure.inbound.rest.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AutoAssignmentResponse(
		boolean assigned,
		UUID orderId,
		UUID assignmentId,
		UUID driverId,
		BigDecimal distanceKm,
		BigDecimal score,
		String orderStatus,
		String reason) {
}
