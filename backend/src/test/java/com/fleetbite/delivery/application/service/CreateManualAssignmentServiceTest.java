package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.DriverNotAssignableException;
import com.fleetbite.delivery.domain.exception.OrderNotAssignableException;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.domain.model.VehicleId;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateManualAssignmentServiceTest {

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
	private com.fleetbite.order.application.service.OrderHistoryRecorder orderHistoryRecorder;

	private CreateManualAssignmentService service;

	@BeforeEach
	void setUp() {
		CreateAssignmentOperation operation = new CreateAssignmentOperation(
				assignmentRepositoryPort,
				orderRepositoryPort,
				driverRepositoryPort,
				orderHistoryRecorder,
				FIXED_CLOCK);
		service = new CreateManualAssignmentService(
				orderRepositoryPort,
				driverRepositoryPort,
				operation);
	}

	@Test
	void execute_shouldAssignReadyOrderAndBusyDriverWithNullScore() {
		Order order = readyOrder();
		Driver driver = availableDriver();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(false);
		when(assignmentRepositoryPort.save(any(DeliveryAssignment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(orderRepositoryPort.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.execute(new CreateManualAssignmentCommand(order.id().value(), driver.id().value()));

		assertEquals(AssignmentStatus.PENDING, result.status());
		assertNull(result.assignmentScore());
		assertEquals(OrderStatus.ASSIGNED, order.status());
		assertEquals(DriverStatus.BUSY, driver.status());
		verify(assignmentRepositoryPort).save(any(DeliveryAssignment.class));
		verify(orderRepositoryPort).update(order);
		verify(driverRepositoryPort).update(driver);
	}

	@Test
	void execute_shouldRejectOrderNotReadyOrWaiting() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-ASG1"),
				"Ana",
				"999",
				"Addr",
				new Location(-12.1, -77.0),
				Money.of(new BigDecimal("10.00")),
				CREATED,
				CREATED.plusMinutes(45));
		Driver driver = availableDriver();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));

		assertThrows(
				OrderNotAssignableException.class,
				() -> service.execute(new CreateManualAssignmentCommand(order.id().value(), driver.id().value())));
		verify(assignmentRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldRejectDriverWithoutLocation() {
		Order order = readyOrder();
		Driver offline = Driver.create(DriverId.generate(), UserId.generate(), "999888778", null, CREATED);
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(offline.id())).thenReturn(Optional.of(offline));

		assertThrows(
				DriverNotAssignableException.class,
				() -> service.execute(new CreateManualAssignmentCommand(order.id().value(), offline.id().value())));
	}

	@Test
	void execute_shouldRejectBusyDriver() {
		Order order = readyOrder();
		Driver driver = availableDriver();
		driver.markBusy(CREATED.plusMinutes(2));
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));

		assertThrows(
				DriverNotAssignableException.class,
				() -> service.execute(new CreateManualAssignmentCommand(order.id().value(), driver.id().value())));
	}

	@Test
	void execute_shouldRejectWhenActiveAssignmentExists() {
		Order order = readyOrder();
		Driver driver = availableDriver();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(true);

		assertThrows(
				ActiveAssignmentAlreadyExistsException.class,
				() -> service.execute(new CreateManualAssignmentCommand(order.id().value(), driver.id().value())));
		verify(assignmentRepositoryPort, never()).save(any());
	}

	@Test
	void execute_shouldAllowWaitingForDriver() {
		Order order = readyOrder();
		order.markWaitingForDriver();
		Driver driver = availableDriver();
		when(orderRepositoryPort.findById(order.id())).thenReturn(Optional.of(order));
		when(driverRepositoryPort.findById(driver.id())).thenReturn(Optional.of(driver));
		when(assignmentRepositoryPort.existsActiveByOrderId(order.id())).thenReturn(false);
		when(assignmentRepositoryPort.save(any(DeliveryAssignment.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(orderRepositoryPort.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(driverRepositoryPort.update(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.execute(new CreateManualAssignmentCommand(order.id().value(), driver.id().value()));

		assertEquals(AssignmentStatus.PENDING, result.status());
		assertEquals(OrderStatus.ASSIGNED, order.status());
	}

	private static Order readyOrder() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-ASG2"),
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
		return order;
	}

	private static Driver availableDriver() {
		Driver driver = Driver.create(
				DriverId.generate(),
				UserId.generate(),
				"999888777",
				new Location(-12.10, -77.03),
				CREATED);
		driver.assignVehicle(VehicleId.generate(), CREATED.plusSeconds(30));
		driver.goOnline(CREATED.plusMinutes(1));
		return driver;
	}
}
