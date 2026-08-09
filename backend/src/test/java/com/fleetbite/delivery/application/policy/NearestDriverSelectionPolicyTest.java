package com.fleetbite.delivery.application.policy;

import com.fleetbite.delivery.application.port.out.DistanceCalculatorPort;
import com.fleetbite.delivery.infrastructure.outbound.geo.HaversineDistanceAdapter;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NearestDriverSelectionPolicyTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 20, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Location DESTINATION = new Location(-12.0464, -77.0428);

	private NearestDriverSelectionPolicy policy;

	@BeforeEach
	void setUp() {
		DistanceCalculatorPort distanceCalculatorPort = new HaversineDistanceAdapter();
		policy = new NearestDriverSelectionPolicy(distanceCalculatorPort);
	}

	@Test
	void select_shouldPickNearestAvailableDriver() {
		Driver near = available(uuid("11111111-1111-1111-1111-111111111111"), new Location(-12.0470, -77.0430));
		Driver far = available(uuid("22222222-2222-2222-2222-222222222222"), new Location(-12.2000, -77.2000));

		Optional<DriverCandidate> selected = policy.select(readyOrder(), List.of(far, near));

		assertTrue(selected.isPresent());
		assertEquals(near.id(), selected.get().driver().id());
		assertEquals(0, selected.get().distanceKm().compareTo(selected.get().score()));
	}

	@Test
	void select_shouldIgnoreOfflineAndBusy() {
		Driver offline = Driver.create(
				DriverId.of(uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
				"Offline",
				"111111111",
				new Location(-12.0470, -77.0430),
				CREATED);
		Driver busy = available(uuid("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), new Location(-12.0470, -77.0430));
		busy.markBusy(CREATED.plusMinutes(2));
		Driver valid = available(uuid("dddddddd-dddd-dddd-dddd-dddddddddddd"), new Location(-12.0500, -77.0500));

		Optional<DriverCandidate> selected = policy.select(
				readyOrder(),
				List.of(offline, busy, valid));

		assertTrue(selected.isPresent());
		assertEquals(valid.id(), selected.get().driver().id());
	}

	@Test
	void select_shouldBreakTiesByUuidNaturalOrder() {
		Location sameSpot = new Location(-12.0500, -77.0500);
		// Java UUID.compareTo uses signed most/least significant bits:
		// ffffffff... < 00000000...
		Driver higherLex = available(uuid("ffffffff-ffff-ffff-ffff-ffffffffffff"), sameSpot);
		Driver lowerLex = available(uuid("00000000-0000-0000-0000-000000000001"), sameSpot);

		Optional<DriverCandidate> selected = policy.select(readyOrder(), List.of(higherLex, lowerLex));

		assertTrue(selected.isPresent());
		assertEquals(higherLex.id(), selected.get().driver().id());
	}

	@Test
	void select_shouldReturnEmptyWhenNoCandidates() {
		assertTrue(policy.select(readyOrder(), List.of()).isEmpty());
	}

	private static Order readyOrder() {
		Order order = Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-POL1"),
				"Ana",
				"999",
				"Addr",
				DESTINATION,
				Money.of(new BigDecimal("10.00")),
				CREATED,
				CREATED.plusMinutes(45));
		order.confirm(CREATED.plusMinutes(1));
		order.startPreparation(CREATED.plusMinutes(2));
		order.markReady(CREATED.plusMinutes(3));
		return order;
	}

	private static Driver available(UUID id, Location location) {
		Driver driver = Driver.create(DriverId.of(id), "Driver", id.toString().substring(0, 9), location, CREATED);
		driver.goOnline(CREATED.plusMinutes(1));
		return driver;
	}

	private static UUID uuid(String value) {
		return UUID.fromString(value);
	}
}
