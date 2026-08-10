package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentLifecycleServicesTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 20, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	@Mock
	private OrderRepositoryPort orderRepositoryPort;
	@Mock
	private DriverRepositoryPort driverRepositoryPort;
	@Mock
	private OrderHistoryRecorder orderHistoryRecorder;

	private RejectAssignmentService rejectService;
	private PickupAssignmentService pickupService;
	private StartDeliveryAssignmentService startDeliveryService;
	private CompleteAssignmentService completeService;
	private AcceptAssignmentService acceptService;

	@BeforeEach
	void setUp() {
		rejectService = new RejectAssignmentService(
				assignmentRepositoryPort,
				orderRepositoryPort,
				driverRepositoryPort,
				orderHistoryRecorder,
				FIXED_CLOCK);
		pickupService = new PickupAssignmentService(
				assignmentRepositoryPort,
				orderRepositoryPort,
				orderHistoryRecorder,
				FIXED_CLOCK);
		startDeliveryService = new StartDeliveryAssignmentService(
				assignmentRepositoryPort,
				orderRepositoryPort,
				orderHistoryRecorder,
				FIXED_CLOCK);
		completeService = new CompleteAssignmentService(
				assignmentRepositoryPort,
				orderRepositoryPort,
				driverRepositoryPort,
				orderHistoryRecorder,
				FIXED_CLOCK);
		acceptService = new AcceptAssignmentService(
				assignmentRepositoryPort,
				orderHistoryRecorder,
				FIXED_CLOCK);
	}

	@Test
	void reject_shouldFreeDriverAndReturnOrderToWaiting() {
		Order order = assignedOrder();
		Driver driver = busyDriver();
		DeliveryAssignment assignment = DeliveryAssignment.create(
				UUID.randomUUID(), order.id(), driver.id(), CREATED.plusMinutes(10));

		when(assignmentRepositoryPort.findById(assignment.id())).thenReturn(Optional.of(assignment));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(assignmentRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));
		when(driverRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = rejectService.execute(assignment.id(), new RejectAssignmentCommand("Vehicle problem"));

		assertEquals(AssignmentStatus.REJECTED, result.status());
		assertEquals(OrderStatus.WAITING_FOR_DRIVER, order.status());
		assertEquals(DriverStatus.AVAILABLE, driver.status());
	}

	@Test
	void pickup_shouldLeaveOrderPickedUp() {
		Order order = assignedOrder();
		Driver driver = busyDriver();
		DeliveryAssignment assignment = DeliveryAssignment.create(
				UUID.randomUUID(), order.id(), driver.id(), CREATED.plusMinutes(10));
		assignment.accept(CREATED.plusMinutes(11));

		when(assignmentRepositoryPort.findById(assignment.id())).thenReturn(Optional.of(assignment));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(assignmentRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = pickupService.execute(assignment.id());

		assertEquals(AssignmentStatus.ACCEPTED, result.status());
		assertEquals(OrderStatus.PICKED_UP, order.status());
	}

	@Test
	void startDelivery_shouldMoveOrderToInTransit() {
		Order order = assignedOrder();
		order.pickUp(CREATED.plusMinutes(12));
		DeliveryAssignment assignment = DeliveryAssignment.create(
				UUID.randomUUID(), order.id(), UUID.randomUUID(), CREATED.plusMinutes(10));
		assignment.accept(CREATED.plusMinutes(11));
		assignment.markPickedUp(CREATED.plusMinutes(12));

		when(assignmentRepositoryPort.findById(assignment.id())).thenReturn(Optional.of(assignment));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		startDeliveryService.execute(assignment.id());

		assertEquals(OrderStatus.IN_TRANSIT, order.status());
	}

	@Test
	void complete_shouldDeliverOrderAndFreeDriver() {
		Order order = assignedOrder();
		order.pickUp(CREATED.plusMinutes(12));
		order.startDelivery(CREATED.plusMinutes(13));
		Driver driver = busyDriver();
		DeliveryAssignment assignment = DeliveryAssignment.create(
				UUID.randomUUID(), order.id(), driver.id(), CREATED.plusMinutes(10));
		assignment.accept(CREATED.plusMinutes(11));
		assignment.markPickedUp(CREATED.plusMinutes(12));

		when(assignmentRepositoryPort.findById(assignment.id())).thenReturn(Optional.of(assignment));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(assignmentRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));
		when(orderRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));
		when(driverRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = completeService.execute(assignment.id());

		assertEquals(AssignmentStatus.COMPLETED, result.status());
		assertEquals(OrderStatus.DELIVERED, order.status());
		assertEquals(DriverStatus.AVAILABLE, driver.status());
	}

	@Test
	void accept_shouldAcceptPendingAssignment() {
		DeliveryAssignment assignment = DeliveryAssignment.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				CREATED.plusMinutes(10));
		when(assignmentRepositoryPort.findById(assignment.id())).thenReturn(Optional.of(assignment));
		when(assignmentRepositoryPort.update(any())).thenAnswer(i -> i.getArgument(0));

		var result = acceptService.execute(assignment.id());

		assertEquals(AssignmentStatus.ACCEPTED, result.status());
	}

	private static Order assignedOrder() {
		Order order = Order.create(
				UUID.randomUUID(),
				OrderCode.of("ORD-2026-LIFE1"),
				"Ana",
				"999",
				"Addr",
				new Location(-12.1, -77.0),
				Money.of(new BigDecimal("10.00")),
				CREATED,
				CREATED.plusMinutes(45));
		order.confirm(CREATED.plusMinutes(1));
		order.startPreparation(CREATED.plusMinutes(2));
		order.markReady(CREATED.plusMinutes(3));
		order.assign(CREATED.plusMinutes(4));
		return order;
	}

	private static Driver busyDriver() {
		Driver driver = Driver.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"999888777",
				new Location(-12.10, -77.03),
				CREATED);
		driver.goOnline(CREATED.plusMinutes(1));
		driver.markBusy(CREATED.plusMinutes(2));
		return driver;
	}
}
