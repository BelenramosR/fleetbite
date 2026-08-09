package com.fleetbite.vehicle.application.service;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.dto.CreateVehicleCommand;
import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.CreateVehicleUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.exception.DuplicateVehiclePlateException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleDataException;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class CreateVehicleService implements CreateVehicleUseCase {

	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final Clock clock;

	public CreateVehicleService(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public VehicleResult execute(CreateVehicleCommand command) {
		Objects.requireNonNull(command, "command is required");
		if (command.type() == null) {
			throw new InvalidVehicleDataException("type is required");
		}

		String plate = requireTrimmed(command.plate(), "plate");
		if (vehicleRepositoryPort.existsByPlate(plate)) {
			throw new DuplicateVehiclePlateException(plate);
		}

		OffsetDateTime createdAt = BusinessTime.toBusinessTime(clock.instant());
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), plate, command.type(), createdAt);
		Vehicle saved = vehicleRepositoryPort.save(vehicle);
		return VehicleResult.from(saved);
	}

	private static String requireTrimmed(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidVehicleDataException(fieldName + " is required");
		}
		return value.trim();
	}
}
