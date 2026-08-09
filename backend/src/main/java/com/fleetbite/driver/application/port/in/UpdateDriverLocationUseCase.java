package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;
import com.fleetbite.driver.domain.model.DriverId;

public interface UpdateDriverLocationUseCase {

	DriverResult execute(DriverId driverId, UpdateDriverLocationCommand command);
}
