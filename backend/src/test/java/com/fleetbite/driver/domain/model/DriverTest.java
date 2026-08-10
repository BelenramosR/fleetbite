package com.fleetbite.driver.domain.model;

import com.fleetbite.driver.domain.exception.DriverNotDeletableException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.exception.InvalidDriverTransitionException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime LATER =
			OffsetDateTime.of(2026, 8, 8, 22, 30, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Location LOCATION = new Location(-12.10, -77.03);
	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Test
	void create_shouldStartOfflineWithoutPhoneOrLocation() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, null, null, CREATED_AT);

		assertEquals(DriverStatus.OFFLINE, driver.status());
		assertNull(driver.phone());
		assertNull(driver.currentLocation());
		assertNull(driver.vehicleId());
		assertEquals(USER_ID, driver.userId());
		assertEquals(CREATED_AT, driver.createdAt());
		assertEquals(CREATED_AT, driver.updatedAt());
	}

	@Test
	void create_shouldAcceptOptionalLocationWhileOffline() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", LOCATION, CREATED_AT);

		assertEquals(DriverStatus.OFFLINE, driver.status());
		assertEquals(LOCATION, driver.currentLocation());
	}

	@Test
	void create_shouldRejectNullUserId() {
		assertThrows(
				InvalidDriverDataException.class,
				() -> Driver.create(UUID.randomUUID(), null, "999888777", null, CREATED_AT));
	}

	@Test
	void updatePhone_shouldChangePhone() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, null, null, CREATED_AT);

		driver.updatePhone("988000111", LATER);

		assertEquals("988000111", driver.phone());
		assertEquals(LATER, driver.updatedAt());
	}

	@Test
	void goOnline_shouldRequirePhone() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, null, LOCATION, CREATED_AT);

		assertThrows(InvalidDriverTransitionException.class, () -> driver.goOnline(LATER));
	}

	@Test
	void goOnline_shouldRequireLocation() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", null, CREATED_AT);

		assertThrows(InvalidDriverTransitionException.class, () -> driver.goOnline(LATER));
	}

	@Test
	void goOnline_shouldSetAvailableWhenLocationPresent() {
		Driver driver = onlineDriver();

		assertEquals(DriverStatus.AVAILABLE, driver.status());
	}

	@Test
	void assignAndUnassignVehicle_shouldToggleHasVehicle() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", null, CREATED_AT);
		UUID vehicleId = UUID.randomUUID();

		driver.assignVehicle(vehicleId, LATER);

		assertTrue(driver.hasVehicle());
		assertEquals(vehicleId, driver.vehicleId());

		driver.unassignVehicle(LATER.plusMinutes(1));

		assertFalse(driver.hasVehicle());
		assertNull(driver.vehicleId());
	}

	@Test
	void ensureDeletable_shouldRejectWhenAvailable() {
		Driver driver = onlineDriver();

		assertThrows(DriverNotDeletableException.class, driver::ensureDeletable);
	}

	@Test
	void ensureDeletable_shouldRejectWhenVehicleAssigned() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", null, CREATED_AT);
		driver.assignVehicle(UUID.randomUUID(), LATER);

		assertThrows(DriverNotDeletableException.class, driver::ensureDeletable);
	}

	@Test
	void ensureDeletable_shouldAllowOfflineWithoutVehicle() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", null, CREATED_AT);

		driver.ensureDeletable();
	}

	@Test
	void markBusyAndAvailable_shouldTransition() {
		Driver driver = onlineDriver();

		driver.markBusy(LATER);
		assertEquals(DriverStatus.BUSY, driver.status());

		driver.markAvailable(LATER.plusMinutes(1));
		assertEquals(DriverStatus.AVAILABLE, driver.status());
	}

	private static Driver onlineDriver() {
		Driver driver = Driver.create(UUID.randomUUID(), USER_ID, "999888777", LOCATION, CREATED_AT);
		driver.goOnline(CREATED_AT.plusMinutes(1));
		return driver;
	}
}
