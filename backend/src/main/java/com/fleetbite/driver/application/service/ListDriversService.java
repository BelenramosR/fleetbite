package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.ListDriversUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;

import java.util.List;
import java.util.Objects;

public final class ListDriversService implements ListDriversUseCase {

	private final DriverRepositoryPort driverRepositoryPort;

	public ListDriversService(DriverRepositoryPort driverRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
	}

	@Override
	public List<DriverResult> execute() {
		return driverRepositoryPort.findAll().stream()
				.map(DriverResult::from)
				.toList();
	}
}
