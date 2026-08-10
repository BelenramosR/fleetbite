package com.fleetbite.driver.application.port.in;

import java.util.UUID;


public interface DeleteDriverUseCase {

	void execute(UUID driverId);
}
