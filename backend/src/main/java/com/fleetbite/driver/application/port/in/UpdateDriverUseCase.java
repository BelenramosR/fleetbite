package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverCommand;
import com.fleetbite.driver.domain.model.DriverId;

public interface UpdateDriverUseCase {

	DriverResult execute(DriverId driverId, UpdateDriverCommand command);
}
