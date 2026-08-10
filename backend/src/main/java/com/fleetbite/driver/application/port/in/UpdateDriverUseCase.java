package com.fleetbite.driver.application.port.in;

import java.util.UUID;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;

public interface UpdateDriverUseCase {

	DriverResult execute(UUID driverId, UpdateDriverCommand command);
}
