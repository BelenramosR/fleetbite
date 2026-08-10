package com.fleetbite.driver.infrastructure.transaction;

import java.util.UUID;

import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.AssignVehicleToDriverUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalAssignVehicleToDriverUseCase implements AssignVehicleToDriverUseCase {

	private final AssignVehicleToDriverUseCase delegate;

	public TransactionalAssignVehicleToDriverUseCase(AssignVehicleToDriverUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public DriverResult execute(UUID driverId, AssignVehicleToDriverCommand command) {
		return delegate.execute(driverId, command);
	}
}
