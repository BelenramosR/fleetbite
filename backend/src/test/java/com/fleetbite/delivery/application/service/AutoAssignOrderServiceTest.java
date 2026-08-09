package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.policy.DriverSelectionPolicy;
import com.fleetbite.delivery.application.policy.NearestDriverSelectionPolicy;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.application.port.out.DistanceCalculatorPort;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.OrderNotAssignableException;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.infrastructure.outbound.geo.HaversineDistanceAdapter;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoAssignOrderServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 20, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);
	private static final Location DESTINATION = new Location(-12.0464, -77.0428);

	@Mock
	private OrderRepositoryPort orderRepositoryPort;
	@Mock
	private DriverRepositoryPort driverRepositoryPort;
	@Mock
	private DeliveryAssignmentRepositoryPort assignmentRepositoryPort;

	private AutoAssignOrderService service;

	@BeforeEach
	void setUp() {
		DistanceCalculatorPort distanceCalculatorPort = new HaversineDistanceAdapter();
		DriverSelectionPolicy policy = new NearestDriverSelectionPolicy(distanceCalculatorPort);
		CreateAssignmentOperation operation = new CreateAssignmentOperation(
				assignmentRepositoryPort,
				orderRepositoryPort,
				driverRepositoryPort,
				FIXED_CLOCK);
		service = new AutoAssignOrderService(
				orderRepositoryPort,
				driverRepositoryPort,
				assignmentRepositoryPort,
				policy,
				operation);
	}

	@Test
	void execute_readyWithDrivers_shouldAssignNearestAndPersistScore() {
		Order order = readyOrder();
		Driver near = available(uuid("11111111-1111-1111-1111-111111111111"), new Location(-12.0470, -77.0430));
		Driver far = available(uuid("22222222-2222-2222-2222-222222222222"), new Location(-12.2000, -77.2000));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(false);
		when(driverRepositoryPort.findAvailableWithLocation()).thenReturn(List.of(far, near));
		when(assignmentRepositoryPort.save(any(DeliveryAssignment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(orderRepositoryPort.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.execute(order.id());

		assertTrue(result.assigned());
		assertEquals(near.id().value(), result.driverId());
		assertEquals(OrderStatus.ASSIGNED, result.orderStatus());
		assertEquals(OrderStatus.ASSIGNED, order.status());
		assertEquals(DriverStatus.BUSY, near.status());
		assertEquals(0, result.distanceKm().compareTo(result.score()));

		ArgumentCaptor<DeliveryAssignment> captor = ArgumentCaptor.forClass(DeliveryAssignment.class);
		verify(assignmentRepositoryPort).save(captor.capture());
		assertEquals(result.score(), captor.getValue().assignmentScore());
	}

	@Test
	void execute_waitingWithDrivers_shouldAssign() {
		Order order = readyOrder();
		order.markWaitingForDriver();
		Driver driver = available(uuid("11111111-1111-1111-1111-111111111111"), new Location(-12.0470, -77.0430));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(false);
		when(driverRepositoryPort.findAvailableWithLocation()).thenReturn(List.of(driver));
		when(assignmentRepositoryPort.save(any(DeliveryAssignment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(orderRepositoryPort.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.execute(order.id());

		assertTrue(result.assigned());
		assertEquals(OrderStatus.ASSIGNED, order.status());
	}

	@Test
	void execute_withoutDrivers_shouldMarkWaiting() {
		Order order = readyOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(false);
		when(driverRepositoryPort.findAvailableWithLocation()).thenReturn(List.of());
		when(orderRepositoryPort.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.execute(order.id());

		assertFalse(result.assigned());
		assertEquals(OrderStatus.WAITING_FOR_DRIVER, result.orderStatus());
		assertEquals("NO_AVAILABLE_DRIVER", result.reason());
		assertNull(result.assignmentId());
		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());
		verify(orderRepositoryPort).update(order);
		verify(assignmentRepositoryPort, never()).save(any());
	}

	@Test
	void execute_alreadyWaitingWithoutDrivers_shouldKeepWaiting() {
		Order order = readyOrder();
		order.markWaitingForDriver();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(false);
		when(driverRepositoryPort.findAvailableWithLocation()).thenReturn(List.of());

		var result = service.execute(order.id());

		assertFalse(result.assigned());
		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());
		verify(orderRepositoryPort, never()).update(any());
	}

	@Test
	void execute_shouldRejectActiveAssignment() {
		Order order = readyOrder();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(true);

		assertThrows(ActiveAssignmentAlreadyExistsException.class, () -> service.execute(order.id()));
	}

	@Test
	void execute_shouldRejectInvalidOrderStatus() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-AUTO1"),
				"Ana",
				"999",
				"Addr",
				DESTINATION,
				Money.of(new BigDecimal("10.00")),
				CREATED,
				CREATED.plusMinutes(45));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));

		assertThrows(OrderNotAssignableException.class, () -> service.execute(order.id()));
	}

	private static Order readyOrder() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-AUTO2"),
				"Ana",
				"999",
				"Addr",
				DESTINATION,
				Money.of(new BigDecimal("10.00")),
				CREATED,
				CREATED.plusMinutes(45));
		order.confirm(CREATED.plusMinutes(1));
		order.startPreparation(CREATED.plusMinutes(2));
		order.markReady(CREATED.plusMinutes(3));
		return order;
	}

	private static Driver available(UUID id, Location location) {
		Driver driver = Driver.create(DriverId.of(id), "Driver", id.toString().substring(0, 9), location, CREATED);
		driver.goOnline(CREATED.plusMinutes(1));
		return driver;
	}

	private static UUID uuid(String value) {
		return UUID.fromString(value);
	}
}
