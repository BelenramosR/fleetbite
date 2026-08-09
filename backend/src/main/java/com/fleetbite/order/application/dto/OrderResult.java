package com.fleetbite.order.application.dto;

import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
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
	private final Instant promisedDeliveryAt;
	private final Instant createdAt;
	private final Instant confirmedAt;
	private final Instant preparationStartedAt;
	private final Instant readyAt;
	private final Instant assignedAt;
	private final Instant pickedUpAt;
	private final Instant inTransitAt;
	private final Instant deliveredAt;
	private final Instant cancelledAt;
	private final Instant failedDeliveryAt;

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
			Instant promisedDeliveryAt,
			Instant createdAt,
			Instant confirmedAt,
			Instant preparationStartedAt,
			Instant readyAt,
			Instant assignedAt,
			Instant pickedUpAt,
			Instant inTransitAt,
			Instant deliveredAt,
			Instant cancelledAt,
			Instant failedDeliveryAt) {
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

	public Instant promisedDeliveryAt() {
		return promisedDeliveryAt;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant confirmedAt() {
		return confirmedAt;
	}

	public Instant preparationStartedAt() {
		return preparationStartedAt;
	}

	public Instant readyAt() {
		return readyAt;
	}

	public Instant assignedAt() {
		return assignedAt;
	}

	public Instant pickedUpAt() {
		return pickedUpAt;
	}

	public Instant inTransitAt() {
		return inTransitAt;
	}

	public Instant deliveredAt() {
		return deliveredAt;
	}

	public Instant cancelledAt() {
		return cancelledAt;
	}

	public Instant failedDeliveryAt() {
		return failedDeliveryAt;
	}
}
