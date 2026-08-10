package com.fleetbite.vehicle.application.service;

import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.VehicleLifecycleUseCase;
import java.util.Objects;
import java.util.UUID;

public final class VehicleLifecycleService implements VehicleLifecycleUseCase {
	private final SendVehicleToMaintenanceService maintenanceOperation;
	private final ActivateVehicleService activateOperation;
	private final DeactivateVehicleService deactivateOperation;

	public VehicleLifecycleService(SendVehicleToMaintenanceService maintenanceOperation,
			ActivateVehicleService activateOperation, DeactivateVehicleService deactivateOperation) {
		this.maintenanceOperation = Objects.requireNonNull(maintenanceOperation);
		this.activateOperation = Objects.requireNonNull(activateOperation);
		this.deactivateOperation = Objects.requireNonNull(deactivateOperation);
	}
	@Override public VehicleResult sendToMaintenance(UUID id) { return maintenanceOperation.execute(id); }
	@Override public VehicleResult activate(UUID id) { return activateOperation.execute(id); }
	@Override public VehicleResult deactivate(UUID id) { return deactivateOperation.execute(id); }
}
