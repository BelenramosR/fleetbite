package com.fleetbite.driver.application.port.in;

import com.fleetbite.driver.application.dto.DriverResult;

import java.util.List;

public interface ListDriversUseCase {

	List<DriverResult> execute();
}
