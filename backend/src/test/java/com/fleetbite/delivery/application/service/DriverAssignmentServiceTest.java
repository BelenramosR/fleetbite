package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.application.exception.ForbiddenOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverAssignmentServiceTest {

	@Mock DriverRepositoryPort drivers;
	@Mock DeliveryAssignmentRepositoryPort assignments;
	@Mock AssignmentWorkflowUseCase workflow;
	@Mock OrderRepositoryPort orders;

	private DriverAssignmentService service;
	private UUID userId;
	private Driver driver;

	@BeforeEach
	void setUp() {
		service = new DriverAssignmentService(drivers, assignments, orders, workflow, java.time.Clock.systemUTC());
		userId = UUID.randomUUID();
		driver = Driver.create(UUID.randomUUID(), userId, "999999999", null, OffsetDateTime.now());
		when(drivers.findByUserId(userId)).thenReturn(Optional.of(driver));
	}

	@Test
	void getActive_shouldOnlySearchByAuthenticatedDriversId() {
		DeliveryAssignment assignment = ownAssignment();
		when(assignments.findActiveByDriverId(driver.id())).thenReturn(Optional.of(assignment));
		when(orders.findById(assignment.orderId())).thenReturn(Optional.of(order(assignment.orderId())));

		var result = service.getActive(userId);

		assertEquals(assignment.id(), result.assignment().id());
		verify(assignments).findActiveByDriverId(driver.id());
	}

	@Test
	void accept_shouldDelegateWhenAssignmentBelongsToAuthenticatedDriver() {
		DeliveryAssignment assignment = ownAssignment();
		AssignmentResult expected = AssignmentResult.from(assignment);
		when(assignments.findById(assignment.id())).thenReturn(Optional.of(assignment));
		when(workflow.accept(assignment.id())).thenReturn(expected);

		assertEquals(expected, service.accept(userId, assignment.id()));
		verify(workflow).accept(assignment.id());
	}

	@Test
	void getSummary_shouldCalculateOnlyAuthenticatedDriversHistory() {
		OffsetDateTime now = OffsetDateTime.parse("2026-08-10T18:00:00-05:00");
		service = new DriverAssignmentService(drivers, assignments, orders, workflow,
				Clock.fixed(Instant.parse("2026-08-10T23:00:00Z"), ZoneOffset.UTC));
		DeliveryAssignment completed = assignmentWithStatus(AssignmentStatus.COMPLETED, now);
		DeliveryAssignment accepted = assignmentWithStatus(AssignmentStatus.ACCEPTED, now.minusHours(1));
		DeliveryAssignment rejected = assignmentWithStatus(AssignmentStatus.REJECTED, now.minusHours(2));
		when(assignments.findAllByDriverId(driver.id()))
				.thenReturn(List.of(completed, accepted, rejected));

		var result = service.getSummary(userId);

		assertEquals(1, result.deliveriesCompletedToday());
		assertEquals(2, result.assignmentsAccepted());
		assertEquals(1, result.assignmentsRejected());
		assertEquals(67, result.acceptanceRate());
		verify(assignments).findAllByDriverId(driver.id());
	}

	@Test
	void everyMutation_shouldRejectAnotherDriversAssignment() {
		DeliveryAssignment foreign = DeliveryAssignment.create(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
		when(assignments.findById(foreign.id())).thenReturn(Optional.of(foreign));

		assertThrows(ForbiddenOperationException.class, () -> service.accept(userId, foreign.id()));
		assertThrows(ForbiddenOperationException.class,
				() -> service.reject(userId, foreign.id(), new RejectAssignmentCommand("No disponible")));
		assertThrows(ForbiddenOperationException.class, () -> service.pickup(userId, foreign.id()));
		assertThrows(ForbiddenOperationException.class, () -> service.startDelivery(userId, foreign.id()));
		assertThrows(ForbiddenOperationException.class, () -> service.complete(userId, foreign.id()));

		verify(workflow, never()).accept(foreign.id());
		verify(workflow, never()).reject(foreign.id(), new RejectAssignmentCommand("No disponible"));
		verify(workflow, never()).pickup(foreign.id());
		verify(workflow, never()).startDelivery(foreign.id());
		verify(workflow, never()).complete(foreign.id());
	}

	private DeliveryAssignment ownAssignment() {
		return DeliveryAssignment.create(
				UUID.randomUUID(), UUID.randomUUID(), driver.id(), OffsetDateTime.now());
	}

	private DeliveryAssignment assignmentWithStatus(AssignmentStatus status, OffsetDateTime timestamp) {
		return DeliveryAssignment.reconstitute(
				UUID.randomUUID(), UUID.randomUUID(), driver.id(), status,
				timestamp.minusMinutes(20),
				status == AssignmentStatus.ACCEPTED || status == AssignmentStatus.COMPLETED ? timestamp.minusMinutes(15) : null,
				status == AssignmentStatus.REJECTED ? timestamp : null,
				status == AssignmentStatus.COMPLETED ? timestamp.minusMinutes(5) : null,
				status == AssignmentStatus.COMPLETED ? timestamp : null,
				status == AssignmentStatus.REJECTED ? "No disponible" : null,
				null, timestamp.minusMinutes(20));
	}

	private Order order(UUID id) {
		OffsetDateTime createdAt = OffsetDateTime.now();
		return Order.create(id, OrderCode.of("ORD-2026-9001"), "Cliente", "999999999",
				"Dirección", new Location(-12.1, -77.0), Money.of(new java.math.BigDecimal("25.00")),
				createdAt, createdAt.plusMinutes(45));
	}
}
