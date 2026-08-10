package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.dto.UpdateDriverLocationCommand;

import java.util.UUID;

public interface DriverSelfUseCase {

	DriverResult getProfile(UUID userId);

	DriverResult updateLocation(UUID userId, UpdateDriverLocationCommand command);

	DriverResult goOnline(UUID userId);

	DriverResult goOffline(UUID userId);
}
