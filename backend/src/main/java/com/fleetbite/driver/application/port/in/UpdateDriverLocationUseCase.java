package com.fleetbite.driver.application.port.in;

import java.util.UUID;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;

public interface UpdateDriverLocationUseCase {

	DriverResult execute(UUID driverId, UpdateDriverLocationCommand command);
}
