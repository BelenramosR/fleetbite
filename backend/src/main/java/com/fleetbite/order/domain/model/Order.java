package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
import com.fleetbite.shared.domain.model.Location;

import java.time.OffsetDateTime;
import java.util.Objects;

public final class Order {

	private final OrderId id;
	private final OrderCode code;
	private String customerName;
	private String customerPhone;
	private String deliveryAddress;
	private Location deliveryLocation;
	private Money totalAmount;
	private final OffsetDateTime promisedDeliveryAt;
	private final OffsetDateTime createdAt;

	private OrderPriority priority;
	private OrderStatus status;
	private OffsetDateTime confirmedAt;
	private OffsetDateTime preparationStartedAt;
	private OffsetDateTime readyAt;
	private OffsetDateTime assignedAt;
	private OffsetDateTime pickedUpAt;
	private OffsetDateTime inTransitAt;
	private OffsetDateTime deliveredAt;
	private OffsetDateTime cancelledAt;
	private OffsetDateTime failedDeliveryAt;

	private Order(
			OrderId id,
			OrderCode code,
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount,
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
		this.deliveryLocation = deliveryLocation;
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

	public static Order create(
			OrderId id,
			OrderCode code,
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount,
			OffsetDateTime createdAt,
			OffsetDateTime promisedDeliveryAt) {
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
				OrderPriority.NORMAL,
				OrderStatus.CREATED,
				promisedDeliveryAt,
				createdAt,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null);
	}

	/**
	 * Rebuilds an existing aggregate from persistence without running the state machine
	 * or altering timestamps.
	 */
	public static Order reconstitute(
			OrderId id,
			OrderCode code,
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount,
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
		if (id == null) {
			throw new InvalidOrderDataException("orderId is required");
		}
		if (code == null) {
			throw new InvalidOrderDataException("orderCode is required");
		}
		if (status == null) {
			throw new InvalidOrderDataException("status is required");
		}
		if (priority == null) {
			throw new InvalidOrderDataException("priority is required");
		}
		if (createdAt == null) {
			throw new InvalidOrderDataException("createdAt is required");
		}
		if (deliveryLocation == null) {
			throw new InvalidOrderDataException("deliveryLocation is required");
		}
		if (totalAmount == null) {
			throw new InvalidOrderDataException("totalAmount is required");
		}
		if (promisedDeliveryAt == null) {
			throw new InvalidOrderDataException("promisedDeliveryAt is required");
		}

		return new Order(
				id,
				code,
				requireText(customerName, "customerName"),
				requireText(customerPhone, "customerPhone"),
				requireText(deliveryAddress, "deliveryAddress"),
				deliveryLocation,
				totalAmount,
				priority,
				status,
				promisedDeliveryAt,
				createdAt,
				confirmedAt,
				preparationStartedAt,
				readyAt,
				assignedAt,
				pickedUpAt,
				inTransitAt,
				deliveredAt,
				cancelledAt,
				failedDeliveryAt);
	}

	public void updateDetails(
			String customerName,
			String customerPhone,
			String deliveryAddress,
			Location deliveryLocation,
			Money totalAmount) {
		ensureEditable();
		if (deliveryLocation == null) {
			throw new InvalidOrderDataException("deliveryLocation is required");
		}
		if (totalAmount == null) {
			throw new InvalidOrderDataException("totalAmount is required");
		}
		this.customerName = requireText(customerName, "customerName");
		this.customerPhone = requireText(customerPhone, "customerPhone");
		this.deliveryAddress = requireText(deliveryAddress, "deliveryAddress");
		this.deliveryLocation = deliveryLocation;
		this.totalAmount = totalAmount;
	}

	public void ensureDeletable() {
		if (status != OrderStatus.CREATED) {
			throw new OrderNotDeletableException(status);
		}
	}

	public void confirm(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.CONFIRMED);
		this.confirmedAt = now;
	}

	private void ensureEditable() {
		if (status != OrderStatus.CREATED && status != OrderStatus.CONFIRMED) {
			throw new OrderNotEditableException(status);
		}
	}

	public void startPreparation(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.PREPARING);
		this.preparationStartedAt = now;
	}

	public void markReady(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.READY);
		this.readyAt = now;
	}

	public void markWaitingForDriver() {
		transitionTo(OrderStatus.WAITING_FOR_DRIVER);
	}

	public void assign(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.ASSIGNED);
		this.assignedAt = now;
	}

	public void pickUp(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.PICKED_UP);
		this.pickedUpAt = now;
	}

	public void startDelivery(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.IN_TRANSIT);
		this.inTransitAt = now;
	}

	public void deliver(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.DELIVERED);
		this.deliveredAt = now;
	}

	public void cancel(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.CANCELLED);
		this.cancelledAt = now;
	}

	public void failDelivery(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(OrderStatus.FAILED_DELIVERY);
		this.failedDeliveryAt = now;
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
			OffsetDateTime promisedDeliveryAt,
			OffsetDateTime createdAt) {
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
		if (createdAt == null) {
			throw new InvalidOrderDataException("createdAt is required");
		}
		if (promisedDeliveryAt == null) {
			throw new InvalidOrderDataException("promisedDeliveryAt is required");
		}
		if (!promisedDeliveryAt.isAfter(createdAt)) {
			throw new InvalidOrderDataException("promisedDeliveryAt must be after createdAt");
		}
	}

	private static void requireTimestamp(OffsetDateTime now) {
		if (now == null) {
			throw new InvalidOrderDataException("timestamp is required");
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
