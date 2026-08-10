package com.fleetbite.delivery.infrastructure.inbound.rest;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.dto.RejectAssignmentCommand;
import com.fleetbite.delivery.application.port.in.AssignmentQueryUseCase;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.DriverNotAssignableException;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiResponseBodyAdvice;
import com.fleetbite.shared.infrastructure.inbound.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({AssignmentHttpMapperImpl.class, GlobalExceptionHandler.class, ApiResponseBodyAdvice.class})
class AssignmentControllerTest {

	private static final OffsetDateTime ASSIGNED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateManualAssignmentUseCase createManualAssignmentUseCase;
	@MockitoBean
	private AutoAssignOrderUseCase autoAssignOrderUseCase;
	@MockitoBean
	private AssignmentQueryUseCase assignmentQueryUseCase;
	@MockitoBean
	private AssignmentWorkflowUseCase assignmentWorkflowUseCase;

	@Test
	void assign_shouldReturn201() throws Exception {
		AssignmentResult result = sampleResult();
		when(createManualAssignmentUseCase.execute(any(CreateManualAssignmentCommand.class))).thenReturn(result);

		mockMvc.perform(post("/api/v1/orders/{orderId}/assign", result.orderId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "driverId": "%s" }
								""".formatted(result.driverId())))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/assignments/" + result.id())))
				.andExpect(jsonPath("$.data.status").value("PENDING"));
	}

	@Test
	void autoAssign_shouldReturn200WhenAssigned() throws Exception {
		UUID orderId = UUID.randomUUID();
		UUID assignmentId = UUID.randomUUID();
		UUID driverId = UUID.randomUUID();
		when(autoAssignOrderUseCase.execute(any())).thenReturn(
				AutoAssignmentResult.assigned(
						orderId,
						assignmentId,
						driverId,
						new java.math.BigDecimal("1.4200"),
						new java.math.BigDecimal("1.4200")));

		mockMvc.perform(post("/api/v1/orders/{orderId}/auto-assign", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.assigned").value(true))
				.andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
				.andExpect(jsonPath("$.data.assignmentId").value(assignmentId.toString()))
				.andExpect(jsonPath("$.data.driverId").value(driverId.toString()))
				.andExpect(jsonPath("$.data.orderStatus").value("ASSIGNED"))
				.andExpect(jsonPath("$.data.score").value(1.4200));
	}

	@Test
	void autoAssign_shouldReturn200WhenNoDriver() throws Exception {
		UUID orderId = UUID.randomUUID();
		when(autoAssignOrderUseCase.execute(any()))
				.thenReturn(AutoAssignmentResult.waitingForDriver(orderId));

		mockMvc.perform(post("/api/v1/orders/{orderId}/auto-assign", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.assigned").value(false))
				.andExpect(jsonPath("$.data.orderStatus").value("WAITING_FOR_DRIVER"))
				.andExpect(jsonPath("$.data.reason").value("NO_AVAILABLE_DRIVER"))
				.andExpect(jsonPath("$.data.assignmentId").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void assign_shouldReturn409WhenActiveExists() throws Exception {
		UUID orderId = UUID.randomUUID();
		when(createManualAssignmentUseCase.execute(any(CreateManualAssignmentCommand.class)))
				.thenThrow(new ActiveAssignmentAlreadyExistsException(orderId));

		mockMvc.perform(post("/api/v1/orders/{orderId}/assign", orderId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "driverId": "%s" }
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ACTIVE_ASSIGNMENT_EXISTS"));
	}

	@Test
	void assign_shouldReturn409WhenDriverNotAssignable() throws Exception {
		UUID orderId = UUID.randomUUID();
		when(createManualAssignmentUseCase.execute(any(CreateManualAssignmentCommand.class)))
				.thenThrow(new DriverNotAssignableException(DriverStatus.OFFLINE));

		mockMvc.perform(post("/api/v1/orders/{orderId}/assign", orderId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "driverId": "%s" }
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DRIVER_NOT_ASSIGNABLE"));
	}

	@Test
	void list_shouldReturn200() throws Exception {
		when(assignmentQueryUseCase.findAll()).thenReturn(List.of(sampleResult()));

		mockMvc.perform(get("/api/v1/assignments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(1)));
	}

	@Test
	void getById_shouldReturn404() throws Exception {
		UUID id = UUID.randomUUID();
		when(assignmentQueryUseCase.getById(id))
				.thenThrow(new ResourceNotFoundException("DeliveryAssignment", id));

		mockMvc.perform(get("/api/v1/assignments/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void reject_shouldReturn400WhenReasonMissing() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/assignments/{id}/reject", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "reason": "   " }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void reject_shouldReturn200() throws Exception {
		AssignmentResult result = rejectedResult();
		when(assignmentWorkflowUseCase.reject(eq(result.id()), any(RejectAssignmentCommand.class)))
				.thenReturn(result);

		mockMvc.perform(post("/api/v1/assignments/{id}/reject", result.id())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "reason": "Vehicle problem" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("REJECTED"));
	}

	@Test
	void accept_pickup_startDelivery_complete_shouldReturn200() throws Exception {
		AssignmentResult result = sampleResult();
		when(assignmentWorkflowUseCase.accept(result.id())).thenReturn(result);
		when(assignmentWorkflowUseCase.pickup(result.id())).thenReturn(result);
		when(assignmentWorkflowUseCase.startDelivery(result.id())).thenReturn(result);
		when(assignmentWorkflowUseCase.complete(result.id())).thenReturn(result);

		mockMvc.perform(post("/api/v1/assignments/{id}/accept", result.id())).andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/assignments/{id}/pickup", result.id())).andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/assignments/{id}/start-delivery", result.id())).andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/assignments/{id}/complete", result.id())).andExpect(status().isOk());
	}

	@Test
	void assign_shouldReturn409OnOptimisticLock() throws Exception {
		UUID orderId = UUID.randomUUID();
		when(createManualAssignmentUseCase.execute(any(CreateManualAssignmentCommand.class)))
				.thenThrow(new OptimisticLockingFailureException("conflict"));

		mockMvc.perform(post("/api/v1/orders/{orderId}/assign", orderId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "driverId": "%s" }
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"));
	}

	private static AssignmentResult sampleResult() {
		return AssignmentResult.from(DeliveryAssignment.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				ASSIGNED_AT));
	}

	private static AssignmentResult rejectedResult() {
		DeliveryAssignment assignment = DeliveryAssignment.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				ASSIGNED_AT);
		assignment.reject("Vehicle problem", ASSIGNED_AT.plusMinutes(1));
		return AssignmentResult.from(assignment);
	}
}
