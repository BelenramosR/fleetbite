package com.fleetbite.delivery.infrastructure.inbound.rest;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.port.in.AcceptAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.CompleteAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.GetAssignmentByIdUseCase;
import com.fleetbite.delivery.application.port.in.ListAssignmentsUseCase;
import com.fleetbite.delivery.application.port.in.PickupAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.RejectAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.StartDeliveryAssignmentUseCase;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.CreateManualAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.request.RejectAssignmentRequest;
import com.fleetbite.delivery.infrastructure.inbound.rest.response.AssignmentResponse;
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
public class AssignmentController {

	private final CreateManualAssignmentUseCase createManualAssignmentUseCase;
	private final GetAssignmentByIdUseCase getAssignmentByIdUseCase;
	private final ListAssignmentsUseCase listAssignmentsUseCase;
	private final AcceptAssignmentUseCase acceptAssignmentUseCase;
	private final RejectAssignmentUseCase rejectAssignmentUseCase;
	private final PickupAssignmentUseCase pickupAssignmentUseCase;
	private final StartDeliveryAssignmentUseCase startDeliveryAssignmentUseCase;
	private final CompleteAssignmentUseCase completeAssignmentUseCase;
	private final AssignmentHttpMapper assignmentHttpMapper;

	public AssignmentController(
			CreateManualAssignmentUseCase createManualAssignmentUseCase,
			GetAssignmentByIdUseCase getAssignmentByIdUseCase,
			ListAssignmentsUseCase listAssignmentsUseCase,
			AcceptAssignmentUseCase acceptAssignmentUseCase,
			RejectAssignmentUseCase rejectAssignmentUseCase,
			PickupAssignmentUseCase pickupAssignmentUseCase,
			StartDeliveryAssignmentUseCase startDeliveryAssignmentUseCase,
			CompleteAssignmentUseCase completeAssignmentUseCase,
			AssignmentHttpMapper assignmentHttpMapper) {
		this.createManualAssignmentUseCase = Objects.requireNonNull(createManualAssignmentUseCase);
		this.getAssignmentByIdUseCase = Objects.requireNonNull(getAssignmentByIdUseCase);
		this.listAssignmentsUseCase = Objects.requireNonNull(listAssignmentsUseCase);
		this.acceptAssignmentUseCase = Objects.requireNonNull(acceptAssignmentUseCase);
		this.rejectAssignmentUseCase = Objects.requireNonNull(rejectAssignmentUseCase);
		this.pickupAssignmentUseCase = Objects.requireNonNull(pickupAssignmentUseCase);
		this.startDeliveryAssignmentUseCase = Objects.requireNonNull(startDeliveryAssignmentUseCase);
		this.completeAssignmentUseCase = Objects.requireNonNull(completeAssignmentUseCase);
		this.assignmentHttpMapper = Objects.requireNonNull(assignmentHttpMapper);
	}

	@PostMapping("/orders/{orderId}/assign")
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

	@GetMapping("/assignments")
	public List<AssignmentResponse> listAssignments() {
		return listAssignmentsUseCase.execute().stream()
				.map(assignmentHttpMapper::toResponse)
				.toList();
	}

	@GetMapping("/assignments/{id}")
	public AssignmentResponse getAssignmentById(@PathVariable UUID id) {
		AssignmentResult result = getAssignmentByIdUseCase.execute(DeliveryAssignmentId.of(id));
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/accept")
	public AssignmentResponse accept(@PathVariable UUID id) {
		AssignmentResult result = acceptAssignmentUseCase.execute(DeliveryAssignmentId.of(id));
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/reject")
	public AssignmentResponse reject(
			@PathVariable UUID id,
			@Valid @RequestBody RejectAssignmentRequest request) {
		AssignmentResult result = rejectAssignmentUseCase.execute(
				DeliveryAssignmentId.of(id),
				assignmentHttpMapper.toCommand(request));
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/pickup")
	public AssignmentResponse pickup(@PathVariable UUID id) {
		AssignmentResult result = pickupAssignmentUseCase.execute(DeliveryAssignmentId.of(id));
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/start-delivery")
	public AssignmentResponse startDelivery(@PathVariable UUID id) {
		AssignmentResult result = startDeliveryAssignmentUseCase.execute(DeliveryAssignmentId.of(id));
		return assignmentHttpMapper.toResponse(result);
	}

	@PostMapping("/assignments/{id}/complete")
	public AssignmentResponse complete(@PathVariable UUID id) {
		AssignmentResult result = completeAssignmentUseCase.execute(DeliveryAssignmentId.of(id));
		return assignmentHttpMapper.toResponse(result);
	}
}
