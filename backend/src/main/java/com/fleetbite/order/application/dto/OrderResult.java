package com.fleetbite.order.application.dto;

import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderResult(
		UUID id,
		String code,
		String customerName,
		String customerPhone,
		String deliveryAddress,
		double deliveryLatitude,
		double deliveryLongitude,
		BigDecimal totalAmount,
		OrderPriority priority,
		OrderStatus status,
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

	public static OrderResult from(Order order) {
		Objects.requireNonNull(order, "order is required");
		return new OrderResult(
				order.id(),
				order.code().value(),
				order.customerName(),
				order.customerPhone(),
				order.deliveryAddress(),
				order.deliveryLocation().latitude(),
				order.deliveryLocation().longitude(),
				order.totalAmount().amount(),
				order.priority(),
				order.status(),
				order.promisedDeliveryAt(),
				order.createdAt(),
				order.confirmedAt(),
				order.preparationStartedAt(),
				order.readyAt(),
				order.assignedAt(),
				order.pickedUpAt(),
				order.inTransitAt(),
				order.deliveredAt(),
				order.cancelledAt(),
				order.failedDeliveryAt());
	}
}
