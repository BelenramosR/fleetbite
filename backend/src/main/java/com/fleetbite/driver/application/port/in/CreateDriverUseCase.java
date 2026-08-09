package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.application.dto.CreateDriverCommand;
import com.fleetbite.driver.application.dto.DriverResult;

public interface CreateDriverUseCase {

	DriverResult execute(CreateDriverCommand command);
}
