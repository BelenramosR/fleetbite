package com.fleetbite.delivery.infrastructure.inbound.rest;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.port.in.AssignmentQueryUseCase;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.CreateManualAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.RejectAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AssignmentResponse;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AutoAssignmentResponse;
import com.fleetbite.shared.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AssignmentController {

	private final CreateManualAssignmentUseCase createManualAssignmentUseCase;
	private final AutoAssignOrderUseCase autoAssignOrderUseCase;
	private final AssignmentQueryUseCase assignmentQueryUseCase;
	private final AssignmentWorkflowUseCase assignmentWorkflowUseCase;
	private final AssignmentHttpMapper assignmentHttpMapper;

	public AssignmentController(
			CreateManualAssignmentUseCase createManualAssignmentUseCase,
			AutoAssignOrderUseCase autoAssignOrderUseCase,
			AssignmentQueryUseCase assignmentQueryUseCase,
			AssignmentWorkflowUseCase assignmentWorkflowUseCase,
			AssignmentHttpMapper assignmentHttpMapper) {
		this.createManualAssignmentUseCase = Objects.requireNonNull(createManualAssignmentUseCase);
		this.autoAssignOrderUseCase = Objects.requireNonNull(autoAssignOrderUseCase);
		this.assignmentQueryUseCase = Objects.requireNonNull(assignmentQueryUseCase);
		this.assignmentWorkflowUseCase = Objects.requireNonNull(assignmentWorkflowUseCase);
		this.assignmentHttpMapper = Objects.requireNonNull(assignmentHttpMapper);
	}

	@PostMapping("/orders/{orderId}/assign")
	@Tag(name = "Orders")
	@Operation(summary = "Assign driver manually",
			description = "ADMIN/DISPATCHER. Creates assignment for a READY or WAITING_FOR_DRIVER order.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Assignment created",
					content = @Content(schema = @Schema(implementation = AssignmentResponse.class))),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Conflict",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public ResponseEntity<AssignmentResponse> assign(
			@PathVariable UUID orderId,
			@Valid @RequestBody CreateManualAssignmentRequest request) {
		AssignmentResult result = createManualAssignmentUseCase.execute(
				assignmentHttpMapper.toCommand(orderId, request));
		URI location = ServletUriComponentsBuilder
				.fromCurrentContextPath()
				.path("/api/v1/assignments/{id}")
				.buildAndExpand(result.id())
				.toUri();
		return ResponseEntity.created(location).body(assignmentHttpMapper.toResponse(result));
	}

	@PostMapping("/orders/{orderId}/auto-assign")
	@Tag(name = "Orders")
	@Operation(summary = "Auto-assign nearest available driver",
			description = """
					ADMIN/DISPATCHER only.
					Candidates: drivers with status AVAILABLE and a known location.
					Distance uses Haversine (km); nearest wins (UUID tie-break).
					assignmentScore currently equals distanceKm.
					If no driver is found: HTTP 200 with assigned=false, reason=NO_AVAILABLE_DRIVER,
					and the order transitions to WAITING_FOR_DRIVER.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Assignment attempted (assigned true or false)",
					content = @Content(schema = @Schema(implementation = AutoAssignmentResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Order not assignable",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AutoAssignmentResponse autoAssign(@PathVariable UUID orderId) {
		AutoAssignmentResult result = autoAssignOrderUseCase.execute(orderId);
		return assignmentHttpMapper.toResponse(result);
	}

	@GetMapping("/assignments")
	@Tag(name = "Assignments")
	@Operation(summary = "List assignments")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Assignments returned"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public List<AssignmentResponse> listAssignments() {
		return assignmentQueryUseCase.findAll().stream()
				.map(assignmentHttpMapper::toResponse)
				.toList();
	}

	@GetMapping("/assignments/{id}")
	@Tag(name = "Assignments")
	@Operation(summary = "Get assignment by id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Assignment found"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AssignmentResponse getAssignmentById(@PathVariable UUID id) {
		AssignmentResult result = assignmentQueryUseCase.getById(id);
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/accept")
	@Tag(name = "Assignments")
	@Operation(summary = "Accept assignment", description = "Assignment PENDING → ACCEPTED")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Assignment accepted"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AssignmentResponse accept(@PathVariable UUID id) {
		AssignmentResult result = assignmentWorkflowUseCase.accept(id);
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/reject")
	@Tag(name = "Assignments")
	@Operation(summary = "Reject assignment", description = "Requires reason. Order returns to WAITING_FOR_DRIVER.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Assignment rejected"),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AssignmentResponse reject(
			@PathVariable UUID id,
			@Valid @RequestBody RejectAssignmentRequest request) {
		AssignmentResult result = assignmentWorkflowUseCase.reject(
				id,
				assignmentHttpMapper.toCommand(request));
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/pickup")
	@Tag(name = "Assignments")
	@Operation(summary = "Mark picked up",
			description = "Requires ACCEPTED assignment. Sets pickedUpAt; order moves to PICKED_UP.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Pickup recorded"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AssignmentResponse pickup(@PathVariable UUID id) {
		AssignmentResult result = assignmentWorkflowUseCase.pickup(id);
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/start-delivery")
	@Tag(name = "Assignments")
	@Operation(summary = "Start delivery",
			description = "Order PICKED_UP → IN_TRANSIT. Assignment remains ACCEPTED until complete.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Delivery started"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AssignmentResponse startDelivery(@PathVariable UUID id) {
		AssignmentResult result = assignmentWorkflowUseCase.startDelivery(id);
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/complete")
	@Tag(name = "Assignments")
	@Operation(summary = "Complete assignment",
			description = "Assignment ACCEPTED → COMPLETED after pickup; order IN_TRANSIT → DELIVERED.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Assignment completed"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public AssignmentResponse complete(@PathVariable UUID id) {
		AssignmentResult result = assignmentWorkflowUseCase.complete(id);
		return assignmentHttpMapper.toResponse(result);
	}
}
