package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.shared.application.exception.ForbiddenOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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

	private DriverAssignmentService service;
	private UUID userId;
	private Driver driver;

	@BeforeEach
	void setUp() {
		service = new DriverAssignmentService(drivers, assignments, workflow);
		userId = UUID.randomUUID();
		driver = Driver.create(UUID.randomUUID(), userId, "999999999", null, OffsetDateTime.now());
		when(drivers.findByUserId(userId)).thenReturn(Optional.of(driver));
	}

	@Test
	void getActive_shouldOnlySearchByAuthenticatedDriversId() {
		DeliveryAssignment assignment = ownAssignment();
		when(assignments.findActiveByDriverId(driver.id())).thenReturn(Optional.of(assignment));

		AssignmentResult result = service.getActive(userId);

		assertEquals(assignment.id(), result.id());
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
}
