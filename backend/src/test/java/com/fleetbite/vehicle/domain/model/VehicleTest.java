package com.fleetbite.vehicle.domain.model;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleDataException;
import com.fleetbite.vehicle.domain.exception.InvalidVehicleTransitionException;
import com.fleetbite.vehicle.domain.exception.VehicleNotDeletableException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime LATER =
			OffsetDateTime.of(2026, 8, 8, 22, 30, 0, 0, BusinessTime.ZONE_OFFSET);

	@Test
	void create_shouldStartActive() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED_AT);

		assertEquals(VehicleStatus.ACTIVE, vehicle.status());
		assertEquals("ABC-123", vehicle.plate());
		assertEquals(VehicleType.MOTORCYCLE, vehicle.type());
		assertEquals(CREATED_AT, vehicle.createdAt());
		assertEquals(CREATED_AT, vehicle.updatedAt());
	}

	@Test
	void create_shouldRejectBlankPlate() {
		assertThrows(
				InvalidVehicleDataException.class,
				() -> Vehicle.create(VehicleId.generate(), "  ", VehicleType.CAR, CREATED_AT));
	}

	@Test
	void create_shouldRejectNullType() {
		assertThrows(
				InvalidVehicleDataException.class,
				() -> Vehicle.create(VehicleId.generate(), "ABC-123", null, CREATED_AT));
	}

	@Test
	void updateDetails_shouldChangePlateAndType() {
		Vehicle vehicle = activeVehicle();

		vehicle.updateDetails("XYZ-999", VehicleType.BICYCLE, LATER);

		assertEquals("XYZ-999", vehicle.plate());
		assertEquals(VehicleType.BICYCLE, vehicle.type());
		assertEquals(LATER, vehicle.updatedAt());
		assertEquals(VehicleStatus.ACTIVE, vehicle.status());
	}

	@Test
	void sendToMaintenance_shouldTransitionActiveToMaintenance() {
		Vehicle vehicle = activeVehicle();

		vehicle.sendToMaintenance(LATER);

		assertEquals(VehicleStatus.MAINTENANCE, vehicle.status());
		assertEquals(LATER, vehicle.updatedAt());
	}

	@Test
	void activate_shouldTransitionMaintenanceToActive() {
		Vehicle vehicle = activeVehicle();
		vehicle.sendToMaintenance(LATER);

		vehicle.activate(LATER.plusMinutes(5));

		assertEquals(VehicleStatus.ACTIVE, vehicle.status());
	}

	@Test
	void deactivate_shouldTransitionActiveToInactive() {
		Vehicle vehicle = activeVehicle();

		vehicle.deactivate(LATER);

		assertEquals(VehicleStatus.INACTIVE, vehicle.status());
	}

	@Test
	void activate_shouldTransitionInactiveToActive() {
		Vehicle vehicle = activeVehicle();
		vehicle.deactivate(LATER);

		vehicle.activate(LATER.plusMinutes(1));

		assertEquals(VehicleStatus.ACTIVE, vehicle.status());
	}

	@Test
	void deactivate_shouldRejectFromMaintenance() {
		Vehicle vehicle = activeVehicle();
		vehicle.sendToMaintenance(LATER);

		assertThrows(InvalidVehicleTransitionException.class, () -> vehicle.deactivate(LATER.plusMinutes(1)));
	}

	@Test
	void sendToMaintenance_shouldRejectFromInactive() {
		Vehicle vehicle = activeVehicle();
		vehicle.deactivate(LATER);

		assertThrows(InvalidVehicleTransitionException.class, () -> vehicle.sendToMaintenance(LATER.plusMinutes(1)));
	}

	@Test
	void ensureDeletable_shouldAllowOnlyInactive() {
		Vehicle active = activeVehicle();
		assertThrows(VehicleNotDeletableException.class, active::ensureDeletable);

		Vehicle maintenance = activeVehicle();
		maintenance.sendToMaintenance(LATER);
		assertThrows(VehicleNotDeletableException.class, maintenance::ensureDeletable);

		Vehicle inactive = activeVehicle();
		inactive.deactivate(LATER);
		inactive.ensureDeletable();
	}

	private static Vehicle activeVehicle() {
		return Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED_AT);
	}
}
