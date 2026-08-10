package com.fleetbite.driver.application.service;

import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;

import java.util.List;
import java.util.Objects;

public final class ListDriversService {

	private final DriverRepositoryPort driverRepositoryPort;
	private final DriverResultAssembler resultAssembler;

	public ListDriversService(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort) {
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort, "driverRepositoryPort");
		this.resultAssembler = new DriverResultAssembler(userRepositoryPort, vehicleRepositoryPort);
	}

	public List<DriverResult> execute() {
		return driverRepositoryPort.findAll().stream()
				.map(resultAssembler::toResult)
				.toList();
	}
}
