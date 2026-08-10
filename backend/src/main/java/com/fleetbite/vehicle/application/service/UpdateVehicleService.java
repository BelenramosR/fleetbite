package com.fleetbite.vehicle.application.service;

import java.util.UUID;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.dto.UpdateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.UpdateVehicleUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.exception.DuplicateVehiclePlateException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleDataException;
import com.fleetbite.vehicle.domain.model.Vehicle;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class UpdateVehicleService implements UpdateVehicleUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final Clock clock;

	public UpdateVehicleService(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public VehicleResult execute(UUID vehicleId, UpdateVehicleCommand command) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");
		Objects.requireNonNull(command, "command is required");
		if (command.type() == null) {
			throw new InvalidVehicleDataException("type is required");
		}

		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

		String plate = requireTrimmed(command.plate(), "plate");
		if (vehicleRepositoryPort.existsByPlateAndIdNot(plate, vehicleId)) {
			throw new DuplicateVehiclePlateException(plate);
		}

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		vehicle.updateDetails(plate, command.type(), now);

		Vehicle updated = vehicleRepositoryPort.update(vehicle);
		return VehicleResult.from(updated);
	}

	private static String requireTrimmed(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidVehicleDataException(fieldName + " is required");
		}
		return value.trim();
	}
}
