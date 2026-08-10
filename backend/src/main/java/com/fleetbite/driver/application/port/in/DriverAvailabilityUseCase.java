package com.fleetbite.driver.application.port.in;
import com.fleetbite.driver.application.dto.DriverResult;
import java.util.UUID;
public interface DriverAvailabilityUseCase {
	DriverResult goOnline(UUID driverId);
	DriverResult goOffline(UUID driverId);
}
