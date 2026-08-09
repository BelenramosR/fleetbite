package com.fleetbite.vehicle.infrastructure.inbound.rest;

import com.fleetbite.shared.infrastructure.config.OpenApiConfig;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.ActivateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.CreateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.DeactivateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.DeleteVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.GetVehicleByIdUseCase;
import com.fleetbite.vehicle.application.port.in.ListVehiclesUseCase;
import com.fleetbite.vehicle.application.port.in.SendVehicleToMaintenanceUseCase;
import com.fleetbite.vehicle.application.port.in.UpdateVehicleUseCase;
import com.fleetbite.vehicle.domain.model.VehicleId;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.CreateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.request.UpdateVehicleRequest;
import com.fleetbite.vehicle.infrastructure.inbound.rest.response.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class VehicleController {

	private final CreateVehicleUseCase createVehicleUseCase;
	private final GetVehicleByIdUseCase getVehicleByIdUseCase;
	private final ListVehiclesUseCase listVehiclesUseCase;
	private final UpdateVehicleUseCase updateVehicleUseCase;
	private final DeleteVehicleUseCase deleteVehicleUseCase;
	private final SendVehicleToMaintenanceUseCase sendVehicleToMaintenanceUseCase;
	private final ActivateVehicleUseCase activateVehicleUseCase;
	private final DeactivateVehicleUseCase deactivateVehicleUseCase;
	private final VehicleHttpMapper vehicleHttpMapper;

	public VehicleController(
			CreateVehicleUseCase createVehicleUseCase,
			GetVehicleByIdUseCase getVehicleByIdUseCase,
			ListVehiclesUseCase listVehiclesUseCase,
			UpdateVehicleUseCase updateVehicleUseCase,
			DeleteVehicleUseCase deleteVehicleUseCase,
			SendVehicleToMaintenanceUseCase sendVehicleToMaintenanceUseCase,
			ActivateVehicleUseCase activateVehicleUseCase,
			DeactivateVehicleUseCase deactivateVehicleUseCase,
			VehicleHttpMapper vehicleHttpMapper) {
		this.createVehicleUseCase = Objects.requireNonNull(createVehicleUseCase);
		this.getVehicleByIdUseCase = Objects.requireNonNull(getVehicleByIdUseCase);
		this.listVehiclesUseCase = Objects.requireNonNull(listVehiclesUseCase);
		this.updateVehicleUseCase = Objects.requireNonNull(updateVehicleUseCase);
		this.deleteVehicleUseCase = Objects.requireNonNull(deleteVehicleUseCase);
		this.sendVehicleToMaintenanceUseCase = Objects.requireNonNull(sendVehicleToMaintenanceUseCase);
		this.activateVehicleUseCase = Objects.requireNonNull(activateVehicleUseCase);
		this.deactivateVehicleUseCase = Objects.requireNonNull(deactivateVehicleUseCase);
		this.vehicleHttpMapper = Objects.requireNonNull(vehicleHttpMapper);
	}

	@GetMapping
	@Operation(summary = "List vehicles")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicles returned"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public List<VehicleResponse> listVehicles() {
		return listVehiclesUseCase.execute().stream()
				.map(vehicleHttpMapper::toResponse)
				.toList();
	}

	@PostMapping
	@Operation(summary = "Create vehicle")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Vehicle created",
					content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Duplicate plate",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody CreateVehicleRequest request) {
		VehicleResult result = createVehicleUseCase.execute(vehicleHttpMapper.toCommand(request));
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(result.id())
				.toUri();
		return ResponseEntity.created(location).body(vehicleHttpMapper.toResponse(result));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get vehicle by id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle found"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public VehicleResponse getVehicleById(@PathVariable UUID id) {
		VehicleResult result = getVehicleByIdUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update vehicle")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle updated"),
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
	public VehicleResponse updateVehicle(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateVehicleRequest request) {
		VehicleResult result = updateVehicleUseCase.execute(VehicleId.of(id), vehicleHttpMapper.toCommand(request));
		return vehicleHttpMapper.toResponse(result);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete vehicle")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Vehicle deleted"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Conflict",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public void deleteVehicle(@PathVariable UUID id) {
		deleteVehicleUseCase.execute(VehicleId.of(id));
	}

	@PostMapping("/{id}/maintenance")
	@Operation(summary = "Send vehicle to maintenance", description = "Status → MAINTENANCE")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle in maintenance"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public VehicleResponse sendToMaintenance(@PathVariable UUID id) {
		VehicleResult result = sendVehicleToMaintenanceUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/activate")
	@Operation(summary = "Activate vehicle", description = "Status → AVAILABLE")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle activated"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public VehicleResponse activate(@PathVariable UUID id) {
		VehicleResult result = activateVehicleUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/deactivate")
	@Operation(summary = "Deactivate vehicle", description = "Status → INACTIVE")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle deactivated"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public VehicleResponse deactivate(@PathVariable UUID id) {
		VehicleResult result = deactivateVehicleUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}
}
