package com.fleetbite.order.application.dto;

import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class OrderResult {

	private final UUID id;
	private final String code;
	private final String customerName;
	private final String customerPhone;
	private final String deliveryAddress;
	private final double deliveryLatitude;
	private final double deliveryLongitude;
	private final BigDecimal totalAmount;
	private final OrderPriority priority;
	private final OrderStatus status;
	private final OffsetDateTime promisedDeliveryAt;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime confirmedAt;
	private final OffsetDateTime preparationStartedAt;
	private final OffsetDateTime readyAt;
	private final OffsetDateTime assignedAt;
	private final OffsetDateTime pickedUpAt;
	private final OffsetDateTime inTransitAt;
	private final OffsetDateTime deliveredAt;
	private final OffsetDateTime cancelledAt;
	private final OffsetDateTime failedDeliveryAt;

	private OrderResult(
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
		this.id = id;
		this.code = code;
		this.customerName = customerName;
		this.customerPhone = customerPhone;
		this.deliveryAddress = deliveryAddress;
		this.deliveryLatitude = deliveryLatitude;
		this.deliveryLongitude = deliveryLongitude;
		this.totalAmount = totalAmount;
		this.priority = priority;
		this.status = status;
		this.promisedDeliveryAt = promisedDeliveryAt;
		this.createdAt = createdAt;
		this.confirmedAt = confirmedAt;
		this.preparationStartedAt = preparationStartedAt;
		this.readyAt = readyAt;
		this.assignedAt = assignedAt;
		this.pickedUpAt = pickedUpAt;
		this.inTransitAt = inTransitAt;
		this.deliveredAt = deliveredAt;
		this.cancelledAt = cancelledAt;
		this.failedDeliveryAt = failedDeliveryAt;
	}

	public static OrderResult from(Order order) {
		Objects.requireNonNull(order, "order is required");
		return new OrderResult(
				order.id().value(),
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

	public UUID id() {
		return id;
	}

	public String code() {
		return code;
	}

	public String customerName() {
		return customerName;
	}

	public String customerPhone() {
		return customerPhone;
	}

	public String deliveryAddress() {
		return deliveryAddress;
	}

	public double deliveryLatitude() {
		return deliveryLatitude;
	}

	public double deliveryLongitude() {
		return deliveryLongitude;
	}

	public BigDecimal totalAmount() {
		return totalAmount;
	}

	public OrderPriority priority() {
		return priority;
	}

	public OrderStatus status() {
		return status;
	}

	public OffsetDateTime promisedDeliveryAt() {
		return promisedDeliveryAt;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime confirmedAt() {
		return confirmedAt;
	}

	public OffsetDateTime preparationStartedAt() {
		return preparationStartedAt;
	}

	public OffsetDateTime readyAt() {
		return readyAt;
	}

	public OffsetDateTime assignedAt() {
		return assignedAt;
	}

	public OffsetDateTime pickedUpAt() {
		return pickedUpAt;
	}

	public OffsetDateTime inTransitAt() {
		return inTransitAt;
	}

	public OffsetDateTime deliveredAt() {
		return deliveredAt;
	}

	public OffsetDateTime cancelledAt() {
		return cancelledAt;
	}

	public OffsetDateTime failedDeliveryAt() {
		return failedDeliveryAt;
	}
}
