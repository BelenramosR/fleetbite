package com.fleetbite.infrastructure.inbound.rest.driver;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.DriverActiveAssignmentResult;
import com.fleetbite.delivery.application.port.in.DriverAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.inbound.rest.AssignmentHttpMapper;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.RejectAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AssignmentResponse;
import com.fleetbite.identity.application.dto.AuthenticatedPrincipal;
import com.fleetbite.order.infrastructure.inbound.rest.OrderHttpMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver/assignments")
public class DriverAssignmentController {

	private final DriverAssignmentUseCase driverAssignmentUseCase;
	private final AssignmentHttpMapper assignmentHttpMapper;
	private final OrderHttpMapper orderHttpMapper;

	public DriverAssignmentController(
			DriverAssignmentUseCase driverAssignmentUseCase,
			AssignmentHttpMapper assignmentHttpMapper,
			OrderHttpMapper orderHttpMapper) {
		this.driverAssignmentUseCase = Objects.requireNonNull(driverAssignmentUseCase);
		this.assignmentHttpMapper = Objects.requireNonNull(assignmentHttpMapper);
		this.orderHttpMapper = Objects.requireNonNull(orderHttpMapper);
	}

	@GetMapping("/active")
	public DriverActiveAssignmentResponse active(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		DriverActiveAssignmentResult result = driverAssignmentUseCase.getActive(principal.userId());
		return new DriverActiveAssignmentResponse(
				assignmentHttpMapper.toResponse(result.assignment()),
				orderHttpMapper.toResponse(result.order()));
	}

	@GetMapping("/summary")
	public DriverAssignmentSummaryResponse summary(
			@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		var result = driverAssignmentUseCase.getSummary(principal.userId());
		return new DriverAssignmentSummaryResponse(
				result.deliveriesCompletedToday(), result.assignmentsAccepted(),
				result.assignmentsRejected(), result.acceptanceRate());
	}

	@PostMapping("/{id}/accept")
	public AssignmentResponse accept(
			@AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
		return toResponse(driverAssignmentUseCase.accept(principal.userId(), id));
	}

	@PostMapping("/{id}/reject")
	public AssignmentResponse reject(
			@AuthenticationPrincipal AuthenticatedPrincipal principal,
			@PathVariable UUID id,
			@Valid @RequestBody RejectAssignmentRequest request) {
		return toResponse(driverAssignmentUseCase.reject(
				principal.userId(), id, assignmentHttpMapper.toCommand(request)));
	}

	@PostMapping("/{id}/pickup")
	public AssignmentResponse pickup(
			@AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
		return toResponse(driverAssignmentUseCase.pickup(principal.userId(), id));
	}

	@PostMapping("/{id}/start-delivery")
	public AssignmentResponse startDelivery(
			@AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
		return toResponse(driverAssignmentUseCase.startDelivery(principal.userId(), id));
	}

	@PostMapping("/{id}/complete")
	public AssignmentResponse complete(
			@AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
		return toResponse(driverAssignmentUseCase.complete(principal.userId(), id));
	}

	private AssignmentResponse toResponse(AssignmentResult result) {
		return assignmentHttpMapper.toResponse(result);
	}
}
