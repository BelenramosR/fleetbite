package com.fleetbite.delivery.application.dto;

import com.fleetbite.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Result of auto-assignment.
 *
 * <p>{@code score} currently equals Haversine {@code distanceKm}. Later phases may evolve score
 * into a weighted value (workload + SLA).
 */
public record AutoAssignmentResult(
		boolean assigned,
		UUID orderId,
		UUID assignmentId,
		UUID driverId,
		BigDecimal distanceKm,
		BigDecimal score,
		OrderStatus orderStatus,
		String reason) {

	public static final String NO_AVAILABLE_DRIVER = "NO_AVAILABLE_DRIVER";

	public static AutoAssignmentResult assigned(
			UUID orderId,
			UUID assignmentId,
			UUID driverId,
			BigDecimal distanceKm,
			BigDecimal score) {
		return new AutoAssignmentResult(
				true,
				orderId,
				assignmentId,
				driverId,
				distanceKm,
				score,
				OrderStatus.ASSIGNED,
				null);
	}

	public static AutoAssignmentResult waitingForDriver(UUID orderId) {
		return new AutoAssignmentResult(
				false,
				orderId,
				null,
				null,
				null,
				null,
				OrderStatus.WAITING_FOR_DRIVER,
				NO_AVAILABLE_DRIVER);
	}
}
