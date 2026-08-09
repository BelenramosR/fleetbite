package com.fleetbite.driver.domain.model;

import com.fleetbite.driver.domain.exception.DriverNotDeletableException;
import com.fleetbite.driver.domain.exception.InvalidDriverDataException;
import com.fleetbite.driver.domain.exception.InvalidDriverTransitionException;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DriverTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime LATER =
			OffsetDateTime.of(2026, 8, 8, 22, 30, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Location LOCATION = new Location(-12.10, -77.03);

	@Test
	void create_shouldStartOfflineWithoutLocation() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", null, CREATED_AT);

		assertEquals(DriverStatus.OFFLINE, driver.status());
		assertNull(driver.currentLocation());
		assertEquals(CREATED_AT, driver.createdAt());
		assertEquals(CREATED_AT, driver.updatedAt());
	}

	@Test
	void create_shouldAcceptOptionalLocationWhileOffline() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", LOCATION, CREATED_AT);

		assertEquals(DriverStatus.OFFLINE, driver.status());
		assertEquals(LOCATION, driver.currentLocation());
	}

	@Test
	void create_shouldRejectBlankName() {
		assertThrows(
				InvalidDriverDataException.class,
				() -> Driver.create(DriverId.generate(), "  ", "999888777", null, CREATED_AT));
	}

	@Test
	void goOnline_shouldTransitionOfflineToAvailableWhenLocationPresent() {
		Driver driver = offlineWithLocation();

		driver.goOnline(LATER);

		assertEquals(DriverStatus.AVAILABLE, driver.status());
		assertEquals(LATER, driver.updatedAt());
	}

	@Test
	void goOnline_shouldRejectWhenLocationMissing() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", null, CREATED_AT);

		InvalidDriverTransitionException exception = assertThrows(
				InvalidDriverTransitionException.class,
				() -> driver.goOnline(LATER));

		assertEquals(DriverStatus.OFFLINE, driver.status());
		assertEquals("INVALID_DRIVER_TRANSITION", exception.getCode());
	}

	@Test
	void goOffline_shouldTransitionAvailableToOffline() {
		Driver driver = offlineWithLocation();
		driver.goOnline(LATER);

		driver.goOffline(LATER.plusMinutes(5));

		assertEquals(DriverStatus.OFFLINE, driver.status());
	}

	@Test
	void markBusy_shouldTransitionAvailableToBusy() {
		Driver driver = offlineWithLocation();
		driver.goOnline(LATER);

		driver.markBusy(LATER.plusMinutes(1));

		assertEquals(DriverStatus.BUSY, driver.status());
	}

	@Test
	void markAvailable_shouldTransitionBusyToAvailable() {
		Driver driver = offlineWithLocation();
		driver.goOnline(LATER);
		driver.markBusy(LATER.plusMinutes(1));

		driver.markAvailable(LATER.plusMinutes(2));

		assertEquals(DriverStatus.AVAILABLE, driver.status());
	}

	@Test
	void goOnline_shouldRejectWhenAlreadyAvailable() {
		Driver driver = offlineWithLocation();
		driver.goOnline(LATER);

		assertThrows(InvalidDriverTransitionException.class, () -> driver.goOnline(LATER.plusMinutes(1)));
	}

	@Test
	void markBusy_shouldRejectFromOffline() {
		Driver driver = offlineWithLocation();

		assertThrows(InvalidDriverTransitionException.class, () -> driver.markBusy(LATER));
	}

	@Test
	void goOffline_shouldRejectFromBusy() {
		Driver driver = offlineWithLocation();
		driver.goOnline(LATER);
		driver.markBusy(LATER.plusMinutes(1));

		assertThrows(InvalidDriverTransitionException.class, () -> driver.goOffline(LATER.plusMinutes(2)));
	}

	@Test
	void updateProfile_shouldChangeNameAndPhone() {
		Driver driver = offlineWithLocation();

		driver.updateProfile("Luis Gomez", "988000111", LATER);

		assertEquals("Luis Gomez", driver.name());
		assertEquals("988000111", driver.phone());
		assertEquals(LATER, driver.updatedAt());
		assertEquals(DriverStatus.OFFLINE, driver.status());
	}

	@Test
	void updateLocation_shouldSetCoordinates() {
		Driver driver = Driver.create(DriverId.generate(), "Carlos Perez", "999888777", null, CREATED_AT);

		driver.updateLocation(LOCATION, LATER);

		assertEquals(LOCATION, driver.currentLocation());
		assertEquals(LATER, driver.updatedAt());
	}

	@Test
	void updateLocation_shouldRejectNull() {
		Driver driver = offlineWithLocation();

		assertThrows(InvalidDriverDataException.class, () -> driver.updateLocation(null, LATER));
	}

	@Test
	void ensureDeletable_shouldAllowOnlyOffline() {
		Driver offline = offlineWithLocation();
		offline.ensureDeletable();

		Driver available = offlineWithLocation();
		available.goOnline(LATER);
		assertThrows(DriverNotDeletableException.class, available::ensureDeletable);

		Driver busy = offlineWithLocation();
		busy.goOnline(LATER);
		busy.markBusy(LATER.plusMinutes(1));
		assertThrows(DriverNotDeletableException.class, busy::ensureDeletable);
	}

	private static Driver offlineWithLocation() {
		return Driver.create(DriverId.generate(), "Carlos Perez", "999888777", LOCATION, CREATED_AT);
	}
}
