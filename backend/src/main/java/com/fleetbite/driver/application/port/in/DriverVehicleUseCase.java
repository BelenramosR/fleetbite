package com.fleetbite.driver.application.port.in;
import com.fleetbite.driver.application.dto.AssignVehicleToDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;
import java.util.UUID;
public interface DriverVehicleUseCase {
	DriverResult assign(UUID driverId, AssignVehicleToDriverCommand command);
	DriverResult unassign(UUID driverId);
}
