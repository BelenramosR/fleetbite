package com.fleetbite.driver.domain.model;

import com.fleetbite.driver.domain.exception.DriverNotDeletableException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.exception.InvalidDriverTransitionException;
import com.fleetbite.shared.domain.model.Location;

import java.time.OffsetDateTime;
import java.util.Objects;

public final class Driver {

	private final DriverId id;
	private String name;
	private String phone;
	private DriverStatus status;
	private Location currentLocation;
	private final OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	private Driver(
			DriverId id,
			String name,
			String phone,
			DriverStatus status,
			Location currentLocation,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.status = status;
		this.currentLocation = currentLocation;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Driver create(
			DriverId id,
			String name,
			String phone,
			Location currentLocation,
			OffsetDateTime createdAt) {
		if (id == null) {
			throw new InvalidDriverDataException("driverId is required");
		}
		if (createdAt == null) {
			throw new InvalidDriverDataException("createdAt is required");
		}
		return new Driver(
				id,
				requireText(name, "name"),
				requireText(phone, "phone"),
				DriverStatus.OFFLINE,
				currentLocation,
				createdAt,
				createdAt);
	}

	public static Driver reconstitute(
			DriverId id,
			String name,
			String phone,
			DriverStatus status,
			Location currentLocation,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		if (id == null) {
			throw new InvalidDriverDataException("driverId is required");
		}
		if (status == null) {
			throw new InvalidDriverDataException("status is required");
		}
		if (createdAt == null) {
			throw new InvalidDriverDataException("createdAt is required");
		}
		if (updatedAt == null) {
			throw new InvalidDriverDataException("updatedAt is required");
		}
		return new Driver(
				id,
				requireText(name, "name"),
				requireText(phone, "phone"),
				status,
				currentLocation,
				createdAt,
				updatedAt);
	}

	public void updateProfile(String name, String phone, OffsetDateTime now) {
		requireTimestamp(now);
		this.name = requireText(name, "name");
		this.phone = requireText(phone, "phone");
		this.updatedAt = now;
	}

	public void updateLocation(Location location, OffsetDateTime now) {
		requireTimestamp(now);
		if (location == null) {
			throw new InvalidDriverDataException("currentLocation is required");
		}
		this.currentLocation = location;
		this.updatedAt = now;
	}

	public void goOnline(OffsetDateTime now) {
		requireTimestamp(now);
		if (currentLocation == null) {
			throw new InvalidDriverTransitionException(
					"Driver cannot go online without a valid currentLocation");
		}
		transitionTo(DriverStatus.AVAILABLE);
		this.updatedAt = now;
	}

	public void goOffline(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(DriverStatus.OFFLINE);
		this.updatedAt = now;
	}

	public void markBusy(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(DriverStatus.BUSY);
		this.updatedAt = now;
	}

	public void markAvailable(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(DriverStatus.AVAILABLE);
		this.updatedAt = now;
	}

	public void ensureDeletable() {
		if (status != DriverStatus.OFFLINE) {
			throw new DriverNotDeletableException(status);
		}
	}

	private void transitionTo(DriverStatus target) {
		if (!canTransition(status, target)) {
			throw new InvalidDriverTransitionException(status, target);
		}
		this.status = target;
	}

	private static boolean canTransition(DriverStatus from, DriverStatus to) {
		return switch (from) {
			case OFFLINE -> to == DriverStatus.AVAILABLE;
			case AVAILABLE -> to == DriverStatus.BUSY || to == DriverStatus.OFFLINE;
			case BUSY -> to == DriverStatus.AVAILABLE;
		};
	}

	private static void requireTimestamp(OffsetDateTime now) {
		if (now == null) {
			throw new InvalidDriverDataException("timestamp is required");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidDriverDataException(fieldName + " is required");
		}
		return value.trim();
	}

	public DriverId id() {
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

	public Location currentLocation() {
		return currentLocation;
	}

	public OffsetDateTime createdAt() {
		return createdAt;
	}

	public OffsetDateTime updatedAt() {
		return updatedAt;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Driver that)) {
			return false;
		}
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
