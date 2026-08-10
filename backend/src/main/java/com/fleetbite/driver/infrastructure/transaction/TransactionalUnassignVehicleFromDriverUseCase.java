package com.fleetbite.driver.infrastructure.transaction;

import java.util.UUID;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.UnassignVehicleFromDriverUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalUnassignVehicleFromDriverUseCase implements UnassignVehicleFromDriverUseCase {

	private final UnassignVehicleFromDriverUseCase delegate;

	public TransactionalUnassignVehicleFromDriverUseCase(UnassignVehicleFromDriverUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public DriverResult execute(UUID driverId) {
		return delegate.execute(driverId);
	}
}
