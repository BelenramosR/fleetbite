package com.fleetbite.driver.application.port.in;
import com.fleetbite.driver.application.dto.DriverResult;
import java.util.List;
import java.util.UUID;
public interface DriverQueryUseCase {
	DriverResult getById(UUID driverId);
	List<DriverResult> findAll();
}
