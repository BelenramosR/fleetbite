package com.fleetbite.driver.infrastructure.inbound.rest;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.AssignVehicleToDriverUseCase;
import com.fleetbite.driver.application.port.in.DeleteDriverUseCase;
import com.fleetbite.driver.application.port.in.GetDriverByIdUseCase;
import com.fleetbite.driver.application.port.in.ListDriversUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOfflineUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOnlineUseCase;
import com.fleetbite.driver.application.port.in.UnassignVehicleFromDriverUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverLocationUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverUseCase;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.infrastructure.inbound.rest.request.AssignVehicleRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverLocationRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.request.UpdateDriverRequest;
import com.fleetbite.driver.infrastructure.inbound.rest.response.DriverResponse;
import com.fleetbite.shared.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Drivers")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DriverController {

	private final GetDriverByIdUseCase getDriverByIdUseCase;
	private final ListDriversUseCase listDriversUseCase;
	private final UpdateDriverUseCase updateDriverUseCase;
	private final DeleteDriverUseCase deleteDriverUseCase;
	private final UpdateDriverLocationUseCase updateDriverLocationUseCase;
	private final SetDriverOnlineUseCase setDriverOnlineUseCase;
	private final SetDriverOfflineUseCase setDriverOfflineUseCase;
	private final AssignVehicleToDriverUseCase assignVehicleToDriverUseCase;
	private final UnassignVehicleFromDriverUseCase unassignVehicleFromDriverUseCase;
	private final DriverHttpMapper driverHttpMapper;

	public DriverController(
			GetDriverByIdUseCase getDriverByIdUseCase,
			ListDriversUseCase listDriversUseCase,
			UpdateDriverUseCase updateDriverUseCase,
			DeleteDriverUseCase deleteDriverUseCase,
			UpdateDriverLocationUseCase updateDriverLocationUseCase,
			SetDriverOnlineUseCase setDriverOnlineUseCase,
			SetDriverOfflineUseCase setDriverOfflineUseCase,
			AssignVehicleToDriverUseCase assignVehicleToDriverUseCase,
			UnassignVehicleFromDriverUseCase unassignVehicleFromDriverUseCase,
			DriverHttpMapper driverHttpMapper) {
		this.getDriverByIdUseCase = Objects.requireNonNull(getDriverByIdUseCase);
		this.listDriversUseCase = Objects.requireNonNull(listDriversUseCase);
		this.updateDriverUseCase = Objects.requireNonNull(updateDriverUseCase);
		this.deleteDriverUseCase = Objects.requireNonNull(deleteDriverUseCase);
		this.updateDriverLocationUseCase = Objects.requireNonNull(updateDriverLocationUseCase);
		this.setDriverOnlineUseCase = Objects.requireNonNull(setDriverOnlineUseCase);
		this.setDriverOfflineUseCase = Objects.requireNonNull(setDriverOfflineUseCase);
		this.assignVehicleToDriverUseCase = Objects.requireNonNull(assignVehicleToDriverUseCase);
		this.unassignVehicleFromDriverUseCase = Objects.requireNonNull(unassignVehicleFromDriverUseCase);
		this.driverHttpMapper = Objects.requireNonNull(driverHttpMapper);
	}

	@GetMapping
	@Operation(summary = "List drivers")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Drivers returned"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public List<DriverResponse> listDrivers() {
		return listDriversUseCase.execute().stream()
				.map(driverHttpMapper::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get driver by id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Driver found"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public DriverResponse getDriverById(@PathVariable UUID id) {
		DriverResult result = getDriverByIdUseCase.execute(DriverId.of(id));
		return driverHttpMapper.toResponse(result);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update driver phone",
			description = "Driver profile is created automatically with POST /users (role=DRIVER). Use this to set phone.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Driver updated"),
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
	public DriverResponse updateDriver(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateDriverRequest request) {
		DriverResult result = updateDriverUseCase.execute(DriverId.of(id), driverHttpMapper.toCommand(request));
		return driverHttpMapper.toResponse(result);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete driver", description = "Only OFFLINE drivers without an assigned vehicle can be deleted.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Driver deleted"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Conflict",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public void deleteDriver(@PathVariable UUID id) {
		deleteDriverUseCase.execute(DriverId.of(id));
	}

	@PutMapping("/{id}/vehicle")
	@Operation(summary = "Assign vehicle to driver", description = "Vehicle must be AVAILABLE and not already assigned.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle assigned"),
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
	public DriverResponse assignVehicle(
			@PathVariable UUID id,
			@Valid @RequestBody AssignVehicleRequest request) {
		DriverResult result = assignVehicleToDriverUseCase.execute(
				DriverId.of(id),
				driverHttpMapper.toCommand(request));
		return driverHttpMapper.toResponse(result);
	}

	@DeleteMapping("/{id}/vehicle")
	@Operation(summary = "Unassign vehicle from driver")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Vehicle unassigned"),
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
	public DriverResponse unassignVehicle(@PathVariable UUID id) {
		DriverResult result = unassignVehicleFromDriverUseCase.execute(DriverId.of(id));
		return driverHttpMapper.toResponse(result);
	}

	@PatchMapping("/{id}/location")
	@Operation(summary = "Update driver location")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Location updated"),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public DriverResponse updateLocation(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateDriverLocationRequest request) {
		DriverResult result = updateDriverLocationUseCase.execute(
				DriverId.of(id),
				driverHttpMapper.toCommand(request));
		return driverHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/online")
	@Operation(summary = "Set driver online",
			description = "Requires phone and current location. Transitions toward AVAILABLE when rules allow.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Driver online"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public DriverResponse goOnline(@PathVariable UUID id) {
		DriverResult result = setDriverOnlineUseCase.execute(DriverId.of(id));
		return driverHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/offline")
	@Operation(summary = "Set driver offline")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Driver offline"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public DriverResponse goOffline(@PathVariable UUID id) {
		DriverResult result = setDriverOfflineUseCase.execute(DriverId.of(id));
		return driverHttpMapper.toResponse(result);
	}
}
