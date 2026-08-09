package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.UnassignVehicleFromDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class UnassignVehicleFromDriverService implements UnassignVehicleFromDriverUseCase {

	private final DriverRepositoryPort driverRepositoryPort;
	private final VehicleRepositoryPort vehicleRepositoryPort;
	private final UserRepositoryPort userRepositoryPort;
	private final Clock clock;

	public UnassignVehicleFromDriverService(
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
	public DriverResult execute(DriverId driverId) {
		Objects.requireNonNull(driverId, "driverId is required");

		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));

		VehicleId vehicleId = driver.vehicleId();
		if (vehicleId == null) {
			throw new InvalidDriverDataException("Driver has no vehicle assigned");
		}

		Vehicle vehicle = vehicleRepositoryPort.findById(vehicleId)
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId.value()));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		driver.unassignVehicle(now);
		vehicle.markAvailableAfterUnassign(now);

		vehicleRepositoryPort.update(vehicle);
		Driver updatedDriver = driverRepositoryPort.update(driver);

		User user = userRepositoryPort.findById(updatedDriver.userId())
				.orElseThrow(() -> new ResourceNotFoundException("User", updatedDriver.userId().value()));
		return DriverResult.from(updatedDriver, user.fullName());
	}
}
