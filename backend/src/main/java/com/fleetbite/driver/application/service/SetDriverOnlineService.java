package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.SetDriverOnlineUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class SetDriverOnlineService implements SetDriverOnlineUseCase {

	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public SetDriverOnlineService(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public DriverResult execute(DriverId driverId) {
		Objects.requireNonNull(driverId, "driverId is required");

		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		driver.goOnline(now);

		Driver updated = driverRepositoryPort.update(driver);
		return DriverResult.from(updated);
	}
}
