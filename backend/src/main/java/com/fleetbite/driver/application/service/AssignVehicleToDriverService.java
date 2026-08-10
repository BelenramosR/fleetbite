package com.fleetbite.driver.application.service;

import java.util.UUID;

import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.VehicleSummary;
import com.fleetbite.driver.application.port.in.AssignVehicleToDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.exception.VehicleAlreadyAssignedException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class AssignVehicleToDriverService implements AssignVehicleToDriverUseCase {

	private final DriverRepositoryPort driverRepositoryPort;
	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final UserRepositoryPort userRepositoryPort;
	private final Clock clock;

	public AssignVehicleToDriverService(
			DriverRepositoryPort driverRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			Clock clock) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.vehicleRepositoryPort = Objects.requireNonNull(vehicleRepositoryPort, "vehicleRepositoryPort");
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort, "userRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public DriverResult execute(UUID driverId, AssignVehicleToDriverCommand command) {
		Objects.requireNonNull(driverId, "driverId is required");
		Objects.requireNonNull(command, "command is required");
		if (command.vehicleId() == null) {
			throw new InvalidDriverDataException("vehicleId is required");
		}

		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
		UUID vehicleId = command.vehicleId();
		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", command.vehicleId()));

		if (driver.hasVehicle()) {
			throw new InvalidDriverDataException("Driver already has a vehicle assigned");
		}

		driverRepositoryPort.findByVehicleId(vehicleId).ifPresent(existing -> {
			throw new VehicleAlreadyAssignedException(command.vehicleId());
		});

		vehicle.ensureAssignable();

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		vehicle.markInUse(now);
		driver.assignVehicle(vehicleId, now);

		Vehicle updatedVehicle = vehicleRepositoryPort.update(vehicle);
		Driver updatedDriver = driverRepositoryPort.update(driver);

		User user = userRepositoryPort.findById(updatedDriver.userId())
				.orElseThrow(() -> new ResourceNotFoundException("User", updatedDriver.userId()));
		return DriverResult.from(updatedDriver, user.fullName(), VehicleSummary.from(updatedVehicle));
	}
}
