package com.fleetbite.driver.application.dto;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class DriverResult {

	private final UUID id;
	private final UUID userId;
	private final String name;
	private final String phone;
	private final DriverStatus status;
	private final Double currentLatitude;
	private final Double currentLongitude;
	private final UUID vehicleId;
	private final VehicleSummary vehicle;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;

	private DriverResult(
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
		this.id = id;
		this.userId = userId;
		this.name = name;
		this.phone = phone;
		this.status = status;
		this.currentLatitude = currentLatitude;
		this.currentLongitude = currentLongitude;
		this.vehicleId = vehicleId;
		this.vehicle = vehicle;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static DriverResult from(Driver driver, String fullName) {
		return from(driver, fullName, null);
	}

	public static DriverResult from(Driver driver, String fullName, VehicleSummary vehicle) {
		Objects.requireNonNull(driver, "driver is required");
		Objects.requireNonNull(fullName, "fullName is required");
		Double latitude = driver.currentLocation() == null ? null : driver.currentLocation().latitude();
		Double longitude = driver.currentLocation() == null ? null : driver.currentLocation().longitude();
		UUID linkedVehicleId = driver.vehicleId() == null ? null : driver.vehicleId().value();
		return new DriverResult(
				driver.id().value(),
				driver.userId().value(),
				fullName,
				driver.phone(),
				driver.status(),
				latitude,
				longitude,
				linkedVehicleId,
				vehicle,
				driver.createdAt(),
				driver.updatedAt());
	}

	public UUID id() {
		return id;
	}

	public UUID userId() {
		return userId;
	}

	public String name() {
		return name;
	}

	public String phone() {
		return phone;
	}

	public DriverStatus status() {
		return status;
	}

	public Double currentLatitude() {
		return currentLatitude;
	}

	public Double currentLongitude() {
		return currentLongitude;
	}

	public UUID vehicleId() {
		return vehicleId;
	}

	public VehicleSummary vehicle() {
		return vehicle;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime updatedAt() {
		return updatedAt;
	}
}
