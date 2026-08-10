package com.fleetbite.driver.application.service;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.DriverQueryUseCase;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
public final class DriverQueryService implements DriverQueryUseCase {
	private final GetDriverByIdService getOperation;
	private final ListDriversService listOperation;
	public DriverQueryService(GetDriverByIdService getOperation, ListDriversService listOperation) {
		this.getOperation = Objects.requireNonNull(getOperation);
		this.listOperation = Objects.requireNonNull(listOperation);
	}
	@Override public DriverResult getById(UUID id) { return getOperation.execute(id); }
	@Override public List<DriverResult> findAll() { return listOperation.execute(); }
}
