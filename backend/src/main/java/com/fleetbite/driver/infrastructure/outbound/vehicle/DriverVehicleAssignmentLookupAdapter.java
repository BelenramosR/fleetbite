package com.fleetbite.driver.infrastructure.outbound.vehicle;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.vehicle.application.port.out.VehicleAssignmentLookupPort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class DriverVehicleAssignmentLookupAdapter implements VehicleAssignmentLookupPort {

	private final DriverRepositoryPort driverRepositoryPort;

	public DriverVehicleAssignmentLookupAdapter(DriverRepositoryPort driverRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
	}

	@Override
	public boolean isAssigned(UUID vehicleId) {
		return driverRepositoryPort.findByVehicleId(vehicleId).isPresent();
	}
}
