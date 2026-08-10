package com.fleetbite.driver.application.dto;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record DriverResult(
		UUID id,
		UUID userId,
		String name,
		String phone,
		DriverStatus status,
		Double currentLatitude,
		Double currentLongitude,
		UUID vehicleId,
		VehicleSummary vehicle,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static DriverResult from(Driver driver, String fullName) {
		return from(driver, fullName, null);
	}

	public static DriverResult from(Driver driver, String fullName, VehicleSummary vehicle) {
		Objects.requireNonNull(driver, "driver is required");
		Objects.requireNonNull(fullName, "fullName is required");
		Double latitude = driver.currentLocation() == null ? null : driver.currentLocation().latitude();
		Double longitude = driver.currentLocation() == null ? null : driver.currentLocation().longitude();
		return new DriverResult(
				driver.id(), driver.userId(), fullName, driver.phone(), driver.status(),
				latitude, longitude, driver.vehicleId(), vehicle, driver.createdAt(), driver.updatedAt());
	}
}
