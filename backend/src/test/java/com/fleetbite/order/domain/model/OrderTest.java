package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;
import com.fleetbite.order.domain.exception.InvalidOrderTransitionException;
import com.fleetbite.shared.domain.model.Location;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

	@Test
	void create_shouldInitializeOrderInCreatedWithNormalPriority() {
		Order order = validOrder();

		assertEquals(OrderStatus.CREATED, order.status());
		assertEquals(OrderPriority.NORMAL, order.priority());
		assertNotNull(order.createdAt());
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
				" ",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("10.00")),
				Instant.now().plus(45, ChronoUnit.MINUTES)));
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
				Instant.now().minus(1, ChronoUnit.MINUTES)));
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
				Instant.now().plus(45, ChronoUnit.MINUTES)));
	}

	@Test
	void confirm_shouldTransitionFromCreatedToConfirmed() {
		Order order = validOrder();

		order.confirm();

		assertEquals(OrderStatus.CONFIRMED, order.status());
		assertNotNull(order.confirmedAt());
	}

	@Test
	void startPreparation_shouldTransitionFromConfirmedToPreparing() {
		Order order = orderIn(OrderStatus.CONFIRMED);

		order.startPreparation();

		assertEquals(OrderStatus.PREPARING, order.status());
		assertNotNull(order.preparationStartedAt());
	}

	@Test
	void markReady_shouldTransitionFromPreparingToReady() {
		Order order = orderIn(OrderStatus.PREPARING);

		order.markReady();

		assertEquals(OrderStatus.READY, order.status());
		assertNotNull(order.readyAt());
	}

	@Test
	void markWaitingForDriverAndAssign_shouldReachAssignedFromReady() {
		// Flujo aprobado: READY -> WAITING_FOR_DRIVER -> ASSIGNED (no hay salto directo)
		Order order = orderIn(OrderStatus.READY);

		order.markWaitingForDriver();
		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());

		order.assign();
		assertEquals(OrderStatus.ASSIGNED, order.status());
		assertNotNull(order.assignedAt());
	}

	@Test
	void assign_shouldRejectDirectTransitionFromReady() {
		Order order = orderIn(OrderStatus.READY);

		assertThrows(InvalidOrderTransitionException.class, order::assign);
		assertEquals(OrderStatus.READY, order.status());
	}

	@Test
	void markPickedUp_shouldTransitionFromAssignedToPickedUp() {
		Order order = orderIn(OrderStatus.ASSIGNED);

		order.markPickedUp();

		assertEquals(OrderStatus.PICKED_UP, order.status());
		assertNotNull(order.pickedUpAt());
	}

	@Test
	void startTransit_shouldTransitionFromPickedUpToInTransit() {
		Order order = orderIn(OrderStatus.PICKED_UP);

		order.startTransit();

		assertEquals(OrderStatus.IN_TRANSIT, order.status());
		assertNotNull(order.inTransitAt());
	}

	@Test
	void markDelivered_shouldTransitionFromInTransitToDelivered() {
		Order order = orderIn(OrderStatus.IN_TRANSIT);

		order.markDelivered();

		assertEquals(OrderStatus.DELIVERED, order.status());
		assertNotNull(order.deliveredAt());
	}

	@Test
	void cancel_shouldBeAllowedFromCreatedConfirmedAndPreparing() {
		Order created = validOrder();
		created.cancel();
		assertEquals(OrderStatus.CANCELLED, created.status());
		assertNotNull(created.cancelledAt());

		Order confirmed = orderIn(OrderStatus.CONFIRMED);
		confirmed.cancel();
		assertEquals(OrderStatus.CANCELLED, confirmed.status());

		Order preparing = orderIn(OrderStatus.PREPARING);
		preparing.cancel();
		assertEquals(OrderStatus.CANCELLED, preparing.status());
	}

	@Test
	void cancel_shouldBeRejectedFromReadyOnwards() {
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.READY).cancel());
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.WAITING_FOR_DRIVER).cancel());
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.ASSIGNED).cancel());
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.PICKED_UP).cancel());
		assertThrows(InvalidOrderTransitionException.class, () -> orderIn(OrderStatus.IN_TRANSIT).cancel());
	}

	@Test
	void markWaitingForDriver_shouldAllowReturnFromAssigned() {
		Order order = orderIn(OrderStatus.ASSIGNED);
		Instant previousAssignedAt = order.assignedAt();

		order.markWaitingForDriver();

		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());
		assertEquals(previousAssignedAt, order.assignedAt());
	}

	@Test
	void markFailedDelivery_shouldOnlyBeAllowedFromInTransit() {
		Order inTransit = orderIn(OrderStatus.IN_TRANSIT);
		inTransit.markFailedDelivery();
		assertEquals(OrderStatus.FAILED_DELIVERY, inTransit.status());
		assertNotNull(inTransit.failedDeliveryAt());

		assertThrows(InvalidOrderTransitionException.class,
				() -> orderIn(OrderStatus.ASSIGNED).markFailedDelivery());
		assertThrows(InvalidOrderTransitionException.class,
				() -> orderIn(OrderStatus.PICKED_UP).markFailedDelivery());
	}

	@Test
	void invalidTransitions_shouldKeepOriginalStatus() {
		Order order = orderIn(OrderStatus.CREATED);

		InvalidOrderTransitionException exception = assertThrows(
				InvalidOrderTransitionException.class,
				order::markReady);

		assertEquals(OrderStatus.CREATED, order.status());
		assertTrue(exception.getCode().equals("INVALID_ORDER_TRANSITION"));
		assertNull(order.readyAt());
	}

	@Test
	void terminalOrders_shouldRejectFurtherModifications() {
		Order delivered = orderIn(OrderStatus.DELIVERED);
		assertThrows(InvalidOrderTransitionException.class, delivered::confirm);
		assertThrows(InvalidOrderTransitionException.class, delivered::cancel);
		assertThrows(InvalidOrderTransitionException.class, delivered::markFailedDelivery);

		Order cancelled = orderIn(OrderStatus.CANCELLED);
		assertThrows(InvalidOrderTransitionException.class, cancelled::confirm);
		assertThrows(InvalidOrderTransitionException.class, cancelled::startPreparation);

		Order failed = orderIn(OrderStatus.FAILED_DELIVERY);
		assertThrows(InvalidOrderTransitionException.class, failed::markDelivered);
		assertThrows(InvalidOrderTransitionException.class, failed::assign);
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
				Instant.now().plus(45, ChronoUnit.MINUTES));
	}

	private static Order orderIn(OrderStatus target) {
		Order order = validOrder();

		if (target == OrderStatus.CREATED) {
			return order;
		}
		if (target == OrderStatus.CANCELLED) {
			order.cancel();
			return order;
		}

		order.confirm();
		if (target == OrderStatus.CONFIRMED) {
			return order;
		}

		order.startPreparation();
		if (target == OrderStatus.PREPARING) {
			return order;
		}

		order.markReady();
		if (target == OrderStatus.READY) {
			return order;
		}

		order.markWaitingForDriver();
		if (target == OrderStatus.WAITING_FOR_DRIVER) {
			return order;
		}

		order.assign();
		if (target == OrderStatus.ASSIGNED) {
			return order;
		}

		order.markPickedUp();
		if (target == OrderStatus.PICKED_UP) {
			return order;
		}

		order.startTransit();
		if (target == OrderStatus.IN_TRANSIT) {
			return order;
		}

		if (target == OrderStatus.DELIVERED) {
			order.markDelivered();
			return order;
		}

		if (target == OrderStatus.FAILED_DELIVERY) {
			order.markFailedDelivery();
			return order;
		}

		throw new IllegalArgumentException("Unsupported target status for test helper: " + target);
	}
}
