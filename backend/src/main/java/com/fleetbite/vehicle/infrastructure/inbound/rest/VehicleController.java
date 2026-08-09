package com.fleetbite.vehicle.infrastructure.inbound.rest;

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
	public List<VehicleResponse> listVehicles() {
		return listVehiclesUseCase.execute().stream()
				.map(vehicleHttpMapper::toResponse)
				.toList();
	}

	@PostMapping
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
	public VehicleResponse getVehicleById(@PathVariable UUID id) {
		VehicleResult result = getVehicleByIdUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}

	@PutMapping("/{id}")
	public VehicleResponse updateVehicle(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateVehicleRequest request) {
		VehicleResult result = updateVehicleUseCase.execute(VehicleId.of(id), vehicleHttpMapper.toCommand(request));
		return vehicleHttpMapper.toResponse(result);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteVehicle(@PathVariable UUID id) {
		deleteVehicleUseCase.execute(VehicleId.of(id));
	}

	@PostMapping("/{id}/maintenance")
	public VehicleResponse sendToMaintenance(@PathVariable UUID id) {
		VehicleResult result = sendVehicleToMaintenanceUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/activate")
	public VehicleResponse activate(@PathVariable UUID id) {
		VehicleResult result = activateVehicleUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/deactivate")
	public VehicleResponse deactivate(@PathVariable UUID id) {
		VehicleResult result = deactivateVehicleUseCase.execute(VehicleId.of(id));
		return vehicleHttpMapper.toResponse(result);
	}
}
