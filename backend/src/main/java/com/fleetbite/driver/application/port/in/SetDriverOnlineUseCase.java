package com.fleetbite.driver.application.port.in;

import java.util.UUID;

import com.fleetbite.driver.application.dto.DriverResult;

public interface SetDriverOnlineUseCase {

	DriverResult execute(UUID driverId);
}
