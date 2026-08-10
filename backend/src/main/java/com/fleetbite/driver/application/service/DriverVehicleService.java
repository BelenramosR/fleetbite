package com.fleetbite.driver.application.service;
import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.DriverVehicleUseCase;
import java.util.Objects;
import java.util.UUID;
public final class DriverVehicleService implements DriverVehicleUseCase {
	private final AssignVehicleToDriverService assignOperation;
	private final UnassignVehicleFromDriverService unassignOperation;
	public DriverVehicleService(AssignVehicleToDriverService assignOperation, UnassignVehicleFromDriverService unassignOperation) {
		this.assignOperation = Objects.requireNonNull(assignOperation);
		this.unassignOperation = Objects.requireNonNull(unassignOperation);
	}
	@Override public DriverResult assign(UUID id, AssignVehicleToDriverCommand command) { return assignOperation.execute(id, command); }
	@Override public DriverResult unassign(UUID id) { return unassignOperation.execute(id); }
}
