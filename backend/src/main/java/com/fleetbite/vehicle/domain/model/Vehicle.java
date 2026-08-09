package com.fleetbite.vehicle.domain.model;

import com.fleetbite.vehicle.domain.exception.InvalidVehicleDataException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleTransitionException;
import com.fleetbite.vehicle.domain.exception.VehicleNotAssignableException;
import com.fleetbite.vehicle.domain.exception.VehicleNotDeletableException;

import java.time.OffsetDateTime;
import java.util.Objects;

public final class Vehicle {

	private final VehicleId id;
	private String plate;
	private VehicleType type;
	private VehicleStatus status;
	private final OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	private Vehicle(
			VehicleId id,
			String plate,
			VehicleType type,
			VehicleStatus status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		this.id = id;
		this.plate = plate;
		this.type = type;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Vehicle create(
			VehicleId id,
			String plate,
			VehicleType type,
			OffsetDateTime createdAt) {
		if (id == null) {
			throw new InvalidVehicleDataException("vehicleId is required");
		}
		if (type == null) {
			throw new InvalidVehicleDataException("type is required");
		}
		if (createdAt == null) {
			throw new InvalidVehicleDataException("createdAt is required");
		}
		return new Vehicle(
				id,
				requireText(plate, "plate"),
				type,
				VehicleStatus.AVAILABLE,
				createdAt,
				createdAt);
	}

	public static Vehicle reconstitute(
			VehicleId id,
			String plate,
			VehicleType type,
			VehicleStatus status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt) {
		if (id == null) {
			throw new InvalidVehicleDataException("vehicleId is required");
		}
		if (type == null) {
			throw new InvalidVehicleDataException("type is required");
		}
		if (status == null) {
			throw new InvalidVehicleDataException("status is required");
		}
		if (createdAt == null) {
			throw new InvalidVehicleDataException("createdAt is required");
		}
		if (updatedAt == null) {
			throw new InvalidVehicleDataException("updatedAt is required");
		}
		return new Vehicle(
				id,
				requireText(plate, "plate"),
				type,
				status,
				createdAt,
				updatedAt);
	}

	public void updateDetails(String plate, VehicleType type, OffsetDateTime now) {
		requireTimestamp(now);
		if (type == null) {
			throw new InvalidVehicleDataException("type is required");
		}
		this.plate = requireText(plate, "plate");
		this.type = type;
		this.updatedAt = now;
	}

	public void sendToMaintenance(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(VehicleStatus.MAINTENANCE);
		this.updatedAt = now;
	}

	public void activate(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(VehicleStatus.AVAILABLE);
		this.updatedAt = now;
	}

	public void deactivate(OffsetDateTime now) {
		requireTimestamp(now);
		transitionTo(VehicleStatus.INACTIVE);
		this.updatedAt = now;
	}

	public void markInUse(OffsetDateTime now) {
		requireTimestamp(now);
		if (status != VehicleStatus.AVAILABLE) {
			throw new VehicleNotAssignableException(status);
		}
		transitionTo(VehicleStatus.IN_USE);
		this.updatedAt = now;
	}

	public void markAvailableAfterUnassign(OffsetDateTime now) {
		requireTimestamp(now);
		if (status != VehicleStatus.IN_USE) {
			throw new InvalidVehicleTransitionException(status, VehicleStatus.AVAILABLE);
		}
		transitionTo(VehicleStatus.AVAILABLE);
		this.updatedAt = now;
	}

	public void ensureAssignable() {
		if (status != VehicleStatus.AVAILABLE) {
			throw new VehicleNotAssignableException(status);
		}
	}

	public void ensureDeletable() {
		if (status != VehicleStatus.INACTIVE) {
			throw new VehicleNotDeletableException(status);
		}
	}

	private void transitionTo(VehicleStatus target) {
		if (!canTransition(status, target)) {
			throw new InvalidVehicleTransitionException(status, target);
		}
		this.status = target;
	}

	private static boolean canTransition(VehicleStatus from, VehicleStatus to) {
		return switch (from) {
			case AVAILABLE -> to == VehicleStatus.MAINTENANCE
					|| to == VehicleStatus.INACTIVE
					|| to == VehicleStatus.IN_USE;
			case IN_USE -> to == VehicleStatus.AVAILABLE;
			case MAINTENANCE -> to == VehicleStatus.AVAILABLE;
			case INACTIVE -> to == VehicleStatus.AVAILABLE;
		};
	}

	private static void requireTimestamp(OffsetDateTime now) {
		if (now == null) {
			throw new InvalidVehicleDataException("timestamp is required");
		}
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new InvalidVehicleDataException(fieldName + " is required");
		}
		return value.trim();
	}

	public VehicleId id() {
		return id;
	}

	public String plate() {
		return plate;
	}

	public VehicleType type() {
		return type;
	}

	public VehicleStatus status() {
		return status;
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
		if (!(other instanceof Vehicle that)) {
			return false;
		}
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
