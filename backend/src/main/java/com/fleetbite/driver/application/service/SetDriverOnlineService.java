package com.fleetbite.driver.application.service;

import java.util.UUID;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class SetDriverOnlineService {

	private final DriverRepositoryPort driverRepositoryPort;
	private final DriverResultAssembler resultAssembler;
	private final Clock clock;

	public SetDriverOnlineService(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			Clock clock) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.resultAssembler = new DriverResultAssembler(userRepositoryPort, vehicleRepositoryPort);
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	public DriverResult execute(UUID driverId) {
		Objects.requireNonNull(driverId, "driverId is required");

		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		driver.goOnline(now);

		Driver updated = driverRepositoryPort.update(driver);
		return resultAssembler.toResult(updated);
	}
}
