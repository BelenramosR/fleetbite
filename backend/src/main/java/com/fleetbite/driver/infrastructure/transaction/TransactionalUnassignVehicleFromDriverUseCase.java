package com.fleetbite.driver.infrastructure.transaction;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.UnassignVehicleFromDriverUseCase;
import com.fleetbite.driver.domain.model.DriverId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalUnassignVehicleFromDriverUseCase implements UnassignVehicleFromDriverUseCase {

	private final UnassignVehicleFromDriverUseCase delegate;

	public TransactionalUnassignVehicleFromDriverUseCase(UnassignVehicleFromDriverUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public DriverResult execute(DriverId driverId) {
		return delegate.execute(driverId);
	}
}
