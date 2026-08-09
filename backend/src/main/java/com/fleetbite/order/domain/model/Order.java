package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.shared.domain.model.Location;

import java.time.Instant;
import java.util.Objects;

public final class Order {

	private final OrderId id;
	private final OrderCode code;
	private final String customerName;
	private final String customerPhone;
	private final String deliveryAddress;
	private final Location deliveryLocation;
	private final Money totalAmount;
	private final Instant promisedDeliveryAt;
	private final Instant createdAt;

	private OrderPriority priority;
	private OrderStatus status;
	private Instant confirmedAt;
	private Instant preparationStartedAt;
	private Instant readyAt;
	private Instant assignedAt;
	private Instant pickedUpAt;
	private Instant inTransitAt;
	private Instant deliveredAt;
	private Instant cancelledAt;
	private Instant failedDeliveryAt;

	private Order(
			OrderId id,
			OrderCode code,
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount,
			Instant promisedDeliveryAt,
			Instant createdAt) {
		this.id = id;
		this.code = code;
		this.customerName = customerName;
		this.customerPhone = customerPhone;
		this.deliveryAddress = deliveryAddress;
		this.deliveryLocation = deliveryLocation;
		this.totalAmount = totalAmount;
		this.promisedDeliveryAt = promisedDeliveryAt;
		this.createdAt = createdAt;
		this.priority = OrderPriority.NORMAL;
		this.status = OrderStatus.CREATED;
	}

	public static Order create(
			OrderId id,
			OrderCode code,
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount,
			Instant promisedDeliveryAt) {
		Instant createdAt = Instant.now();
		validateCreation(
				id,
				code,
				customerName,
				customerPhone,
				deliveryAddress,
				deliveryLocation,
				totalAmount,
				promisedDeliveryAt,
				createdAt);
		return new Order(
				id,
				code,
				requireText(customerName, "customerName"),
				requireText(customerPhone, "customerPhone"),
				requireText(deliveryAddress, "deliveryAddress"),
				deliveryLocation,
				totalAmount,
				promisedDeliveryAt,
				createdAt);
	}

	public void confirm() {
		transitionTo(OrderStatus.CONFIRMED);
		this.confirmedAt = Instant.now();
	}

	public void startPreparation() {
		transitionTo(OrderStatus.PREPARING);
		this.preparationStartedAt = Instant.now();
	}

	public void markReady() {
		transitionTo(OrderStatus.READY);
		this.readyAt = Instant.now();
	}

	public void markWaitingForDriver() {
		transitionTo(OrderStatus.WAITING_FOR_DRIVER);
	}

	public void assign() {
		transitionTo(OrderStatus.ASSIGNED);
		this.assignedAt = Instant.now();
	}

	public void markPickedUp() {
		transitionTo(OrderStatus.PICKED_UP);
		this.pickedUpAt = Instant.now();
	}

	public void startTransit() {
		transitionTo(OrderStatus.IN_TRANSIT);
		this.inTransitAt = Instant.now();
	}

	public void markDelivered() {
		transitionTo(OrderStatus.DELIVERED);
		this.deliveredAt = Instant.now();
	}

	public void cancel() {
		transitionTo(OrderStatus.CANCELLED);
		this.cancelledAt = Instant.now();
	}

	public void markFailedDelivery() {
		transitionTo(OrderStatus.FAILED_DELIVERY);
		this.failedDeliveryAt = Instant.now();
	}

	public OrderId id() {
		return id;
	}

	public OrderCode code() {
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

	public Location deliveryLocation() {
		return deliveryLocation;
	}

	public Money totalAmount() {
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

	private void transitionTo(OrderStatus target) {
		if (!isTransitionAllowed(this.status, target)) {
			throw new InvalidOrderTransitionException(this.status, target);
		}
		this.status = target;
	}

	private static boolean isTransitionAllowed(OrderStatus from, OrderStatus to) {
		return switch (from) {
			case CREATED -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
			case CONFIRMED -> to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED;
			case PREPARING -> to == OrderStatus.READY || to == OrderStatus.CANCELLED;
			case READY -> to == OrderStatus.WAITING_FOR_DRIVER;
			case WAITING_FOR_DRIVER -> to == OrderStatus.ASSIGNED;
			case ASSIGNED -> to == OrderStatus.PICKED_UP || to == OrderStatus.WAITING_FOR_DRIVER;
			case PICKED_UP -> to == OrderStatus.IN_TRANSIT;
			case IN_TRANSIT -> to == OrderStatus.DELIVERED || to == OrderStatus.FAILED_DELIVERY;
			case DELIVERED, CANCELLED, FAILED_DELIVERY -> false;
		};
	}

	private static void validateCreation(
			OrderId id,
			OrderCode code,
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount,
			Instant promisedDeliveryAt,
			Instant createdAt) {
		if (id == null) {
			throw new InvalidOrderDataException("orderId is required");
		}
		if (code == null) {
			throw new InvalidOrderDataException("orderCode is required");
		}
		requireText(customerName, "customerName");
		requireText(customerPhone, "customerPhone");
		requireText(deliveryAddress, "deliveryAddress");
		if (deliveryLocation == null) {
			throw new InvalidOrderDataException("deliveryLocation is required");
		}
		if (totalAmount == null) {
			throw new InvalidOrderDataException("totalAmount is required");
		}
		if (promisedDeliveryAt == null) {
			throw new InvalidOrderDataException("promisedDeliveryAt is required");
		}
		if (!promisedDeliveryAt.isAfter(createdAt)) {
			throw new InvalidOrderDataException("promisedDeliveryAt must be after createdAt");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidOrderDataException(fieldName + " is required");
		}
		return value.trim();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Order that)) {
			return false;
		}
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
