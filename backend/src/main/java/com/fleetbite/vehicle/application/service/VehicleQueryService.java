package com.fleetbite.vehicle.application.service;

import com.fleetbite.vehicle.application.dto.VehicleResult;
import com.fleetbite.vehicle.application.port.in.VehicleQueryUseCase;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class VehicleQueryService implements VehicleQueryUseCase {
	private final GetVehicleByIdService getOperation;
	private final ListVehiclesService listOperation;

	public VehicleQueryService(GetVehicleByIdService getOperation, ListVehiclesService listOperation) {
		this.getOperation = Objects.requireNonNull(getOperation);
		this.listOperation = Objects.requireNonNull(listOperation);
	}
	@Override public VehicleResult getById(UUID id) { return getOperation.execute(id); }
	@Override public List<VehicleResult> findAll() { return listOperation.execute(); }
}
