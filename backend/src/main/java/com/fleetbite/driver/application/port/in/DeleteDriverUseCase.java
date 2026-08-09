package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.domain.model.DriverId;

public interface DeleteDriverUseCase {

	void execute(DriverId driverId);
}
