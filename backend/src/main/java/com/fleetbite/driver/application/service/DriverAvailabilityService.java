package com.fleetbite.driver.application.service;
import com.fleetbite.driver.application.dto.DriverResult;
import com.fleetbite.driver.application.port.in.DriverAvailabilityUseCase;
import java.util.Objects;
import java.util.UUID;
public final class DriverAvailabilityService implements DriverAvailabilityUseCase {
	private final SetDriverOnlineService onlineOperation;
	private final SetDriverOfflineService offlineOperation;
	public DriverAvailabilityService(SetDriverOnlineService onlineOperation, SetDriverOfflineService offlineOperation) {
		this.onlineOperation = Objects.requireNonNull(onlineOperation);
		this.offlineOperation = Objects.requireNonNull(offlineOperation);
	}
	@Override public DriverResult goOnline(UUID id) { return onlineOperation.execute(id); }
	@Override public DriverResult goOffline(UUID id) { return offlineOperation.execute(id); }
}
