package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.domain.model.DriverId;

public interface GetDriverByIdUseCase {

	DriverResult execute(DriverId driverId);
}
