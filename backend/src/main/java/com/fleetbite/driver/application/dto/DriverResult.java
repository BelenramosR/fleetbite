package com.fleetbite.driver.application.dto;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class DriverResult {

	private final UUID id;
	private final String name;
	private final String phone;
	private final DriverStatus status;
	private final Double currentLatitude;
	private final Double currentLongitude;
	private final OffsetDateTime createdAt;
	private final OffsetDateTime updatedAt;

	private DriverResult(
			UUID id,
			String name,
			String phone,
			DriverStatus status,
			Double currentLatitude,
			Double currentLongitude,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.status = status;
		this.currentLatitude = currentLatitude;
		this.currentLongitude = currentLongitude;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static DriverResult from(Driver driver) {
		Objects.requireNonNull(driver, "driver is required");
		Double latitude = driver.currentLocation() == null ? null : driver.currentLocation().latitude();
		Double longitude = driver.currentLocation() == null ? null : driver.currentLocation().longitude();
		return new DriverResult(
				driver.id().value(),
				driver.name(),
				driver.phone(),
				driver.status(),
				latitude,
				longitude,
				driver.createdAt(),
				driver.updatedAt());
	}

	public UUID id() {
		return id;
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

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime updatedAt() {
		return updatedAt;
	}
}
