package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.order.domain.exception.OrderNotDeletableException;
import com.fleetbite.order.domain.exception.OrderNotEditableException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime LATER =
			OffsetDateTime.of(2026, 8, 8, 23, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Test
	void create_shouldInitializeOrderInCreatedWithNormalPriority() {
		Order order = validOrder();

		assertEquals(OrderStatus.CREATED, order.status());
		assertEquals(OrderPriority.NORMAL, order.priority());
		assertEquals(CREATED_AT, order.createdAt());
		assertEquals(PROMISED_AT, order.promisedDeliveryAt());
		assertNull(order.confirmedAt());
		assertNull(order.preparationStartedAt());
		assertNull(order.readyAt());
		assertNull(order.assignedAt());
		assertNull(order.pickedUpAt());
		assertNull(order.inTransitAt());
		assertNull(order.deliveredAt());
		assertNull(order.cancelledAt());
		assertNull(order.failedDeliveryAt());
		assertEquals("Ana Torres", order.customerName());
		assertEquals(Money.of(new BigDecimal("85.90")), order.totalAmount());
	}

	@Test
	void create_shouldRejectBlankCustomerName() {
		assertThrows(InvalidOrderDataException.class, () -> Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-0001"),
				"   ",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("10.00")),
				CREATED_AT,
				PROMISED_AT));
	}

	@Test
	void create_shouldRejectNegativeAmount() {
		assertThrows(InvalidOrderDataException.class, () -> Money.of(new BigDecimal("-1.00")));
	}

	@Test
	void create_shouldRejectPromisedDeliveryAtNotAfterCreatedAt() {
		assertThrows(InvalidOrderDataException.class, () -> Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-0001"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("10.00")),
				CREATED_AT,
				CREATED_AT.minusMinutes(1)));
	}

	@Test
	void create_shouldRejectNullDeliveryLocation() {
		assertThrows(InvalidOrderDataException.class, () -> Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-0001"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				null,
				Money.of(new BigDecimal("10.00")),
				CREATED_AT,
				PROMISED_AT));
	}

	@Test
	void confirm_shouldTransitionFromCreatedToConfirmed() {
		Order order = validOrder();

		order.confirm(LATER);

		assertEquals(OrderStatus.CONFIRMED, order.status());
		assertEquals(LATER, order.confirmedAt());
	}

	@Test
	void startPreparation_shouldTransitionFromConfirmedToPreparing() {
		Order order = orderIn(OrderStatus.CONFIRMED);

		order.startPreparation(LATER);

		assertEquals(OrderStatus.PREPARING, order.status());
		assertEquals(LATER, order.preparationStartedAt());
	}

	@Test
	void markReady_shouldTransitionFromPreparingToReady() {
		Order order = orderIn(OrderStatus.PREPARING);

		order.markReady(LATER);

		assertEquals(OrderStatus.READY, order.status());
		assertEquals(LATER, order.readyAt());
	}

	@Test
	void markWaitingForDriverAndAssign_shouldReachAssignedFromReady() {
		Order order = orderIn(OrderStatus.READY);

		order.markWaitingForDriver();
		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());

		order.assign(LATER);
		assertEquals(OrderStatus.ASSIGNED, order.status());
		assertEquals(LATER, order.assignedAt());
	}

	@Test
	void assign_shouldRejectDirectTransitionFromReady() {
		Order order = orderIn(OrderStatus.READY);

		assertThrows(InvalidOrderTransitionException.class, () -> order.assign(LATER));
		assertEquals(OrderStatus.READY, order.status());
	}

	@Test
	void pickUp_shouldTransitionFromAssignedToPickedUp() {
		Order order = orderIn(OrderStatus.ASSIGNED);

		order.pickUp(LATER);

		assertEquals(OrderStatus.PICKED_UP, order.status());
		assertEquals(LATER, order.pickedUpAt());
	}

	@Test
	void startDelivery_shouldTransitionFromPickedUpToInTransit() {
		Order order = orderIn(OrderStatus.PICKED_UP);

		order.startDelivery(LATER);

		assertEquals(OrderStatus.IN_TRANSIT, order.status());
		assertEquals(LATER, order.inTransitAt());
	}

	@Test
	void deliver_shouldTransitionFromInTransitToDelivered() {
		Order order = orderIn(OrderStatus.IN_TRANSIT);

		order.deliver(LATER);

		assertEquals(OrderStatus.DELIVERED, order.status());
		assertEquals(LATER, order.deliveredAt());
	}

	@Test
	void cancel_shouldBeAllowedFromCreatedConfirmedAndPreparing() {
		Order created = validOrder();
		created.cancel(LATER);
		assertEquals(OrderStatus.CANCELLED, created.status());
		assertEquals(LATER, created.cancelledAt());

		Order confirmed = orderIn(OrderStatus.CONFIRMED);
		confirmed.cancel(LATER);
		assertEquals(OrderStatus.CANCELLED, confirmed.status());

		Order preparing = orderIn(OrderStatus.PREPARING);
		preparing.cancel(LATER);
		assertEquals(OrderStatus.CANCELLED, preparing.status());
	}

	@Test
	void cancel_shouldBeRejectedFromReadyOnwards() {
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.READY).cancel(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.WAITING_FOR_DRIVER).cancel(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.ASSIGNED).cancel(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.PICKED_UP).cancel(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.IN_TRANSIT).cancel(LATER));
	}

	@Test
	void markWaitingForDriver_shouldAllowReturnFromAssigned() {
		Order order = orderIn(OrderStatus.ASSIGNED);
		OffsetDateTime previousAssignedAt = order.assignedAt();

		order.markWaitingForDriver();

		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());
		assertEquals(previousAssignedAt, order.assignedAt());
	}

	@Test
	void failDelivery_shouldOnlyBeAllowedFromInTransit() {
		Order inTransit = orderIn(OrderStatus.IN_TRANSIT);
		inTransit.failDelivery(LATER);
		assertEquals(OrderStatus.FAILED_DELIVERY, inTransit.status());
		assertEquals(LATER, inTransit.failedDeliveryAt());

		assertThrows(InvalidOrderTransitionException.class,
				() -> orderIn(OrderStatus.ASSIGNED).failDelivery(LATER));
		assertThrows(InvalidOrderTransitionException.class,
				() -> orderIn(OrderStatus.PICKED_UP).failDelivery(LATER));
	}

	@Test
	void invalidTransitions_shouldKeepOriginalStatus() {
		Order order = orderIn(OrderStatus.CREATED);

		InvalidOrderTransitionException exception = assertThrows(
				InvalidOrderTransitionException.class,
				() -> order.markReady(LATER));

		assertEquals(OrderStatus.CREATED, order.status());
		assertTrue(exception.getCode().equals("INVALID_ORDER_TRANSITION"));
		assertNull(order.readyAt());
	}

	@Test
	void terminalOrders_shouldRejectFurtherModifications() {
		Order delivered = orderIn(OrderStatus.DELIVERED);
		assertThrows(InvalidOrderTransitionException.class, () -> delivered.confirm(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> delivered.cancel(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> delivered.failDelivery(LATER));

		Order cancelled = orderIn(OrderStatus.CANCELLED);
		assertThrows(InvalidOrderTransitionException.class, () -> cancelled.confirm(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> cancelled.startPreparation(LATER));

		Order failed = orderIn(OrderStatus.FAILED_DELIVERY);
		assertThrows(InvalidOrderTransitionException.class, () -> failed.deliver(LATER));
		assertThrows(InvalidOrderTransitionException.class, () -> failed.assign(LATER));
	}

	@Test
	void updateDetails_shouldUpdateEditableFieldsInCreated() {
		Order order = validOrder();
		OrderId id = order.id();
		OrderCode code = order.code();

		order.updateDetails(
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				new Location(-12.11, -77.03),
				Money.of(new BigDecimal("99.50")));

		assertEquals("Luis Gomez", order.customerName());
		assertEquals("988000111", order.customerPhone());
		assertEquals("Calle Nueva 10", order.deliveryAddress());
		assertEquals(-12.11, order.deliveryLocation().latitude());
		assertEquals(0, order.totalAmount().amount().compareTo(new BigDecimal("99.50")));
		assertEquals(id, order.id());
		assertEquals(code, order.code());
		assertEquals(OrderStatus.CREATED, order.status());
		assertEquals(OrderPriority.NORMAL, order.priority());
		assertEquals(CREATED_AT, order.createdAt());
		assertEquals(PROMISED_AT, order.promisedDeliveryAt());
	}

	@Test
	void updateDetails_shouldAllowConfirmedOrders() {
		Order order = orderIn(OrderStatus.CONFIRMED);

		order.updateDetails(
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				new Location(-12.11, -77.03),
				Money.of(new BigDecimal("40.00")));

		assertEquals("Luis Gomez", order.customerName());
		assertEquals(OrderStatus.CONFIRMED, order.status());
	}

	@Test
	void updateDetails_shouldRejectPreparingAndLater() {
		Order preparing = orderIn(OrderStatus.PREPARING);
		assertThrows(OrderNotEditableException.class, () -> preparing.updateDetails(
				"Luis Gomez",
				"988000111",
				"Calle Nueva 10",
				new Location(-12.11, -77.03),
				Money.of(new BigDecimal("40.00"))));
	}

	@Test
	void ensureDeletable_shouldAllowCreatedOnly() {
		Order created = validOrder();
		created.ensureDeletable();

		Order confirmed = orderIn(OrderStatus.CONFIRMED);
		assertThrows(OrderNotDeletableException.class, confirmed::ensureDeletable);
	}

	private static Order validOrder() {
		return Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-0001"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}

	private static Order orderIn(OrderStatus target) {
		Order order = validOrder();
		OffsetDateTime step = CREATED_AT.plusMinutes(1);

		if (target == OrderStatus.CREATED) {
			return order;
		}
		if (target == OrderStatus.CANCELLED) {
			order.cancel(step);
			return order;
		}

		order.confirm(step);
		step = step.plusMinutes(1);
		if (target == OrderStatus.CONFIRMED) {
			return order;
		}

		order.startPreparation(step);
		step = step.plusMinutes(1);
		if (target == OrderStatus.PREPARING) {
			return order;
		}

		order.markReady(step);
		step = step.plusMinutes(1);
		if (target == OrderStatus.READY) {
			return order;
		}

		order.markWaitingForDriver();
		if (target == OrderStatus.WAITING_FOR_DRIVER) {
			return order;
		}

		order.assign(step);
		step = step.plusMinutes(1);
		if (target == OrderStatus.ASSIGNED) {
			return order;
		}

		order.pickUp(step);
		step = step.plusMinutes(1);
		if (target == OrderStatus.PICKED_UP) {
			return order;
		}

		order.startDelivery(step);
		step = step.plusMinutes(1);
		if (target == OrderStatus.IN_TRANSIT) {
			return order;
		}

		if (target == OrderStatus.DELIVERED) {
			order.deliver(step);
			return order;
		}

		if (target == OrderStatus.FAILED_DELIVERY) {
			order.failDelivery(step);
			return order;
		}

		throw new IllegalArgumentException("Unsupported target status for test helper: " + target);
	}
}
