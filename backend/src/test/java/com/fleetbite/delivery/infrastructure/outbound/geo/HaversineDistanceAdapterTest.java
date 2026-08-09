package com.fleetbite.delivery.infrastructure.outbound.geo;

import com.fleetbite.shared.domain.model.Location;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaversineDistanceAdapterTest {

	private final HaversineDistanceAdapter adapter = new HaversineDistanceAdapter();

	@Test
	void sameLocation_shouldBeApproximatelyZero() {
		Location point = new Location(-12.0464, -77.0428);

		BigDecimal distance = adapter.calculateKm(point, point);

		assertTrue(distance.compareTo(new BigDecimal("0.0001")) <= 0);
	}

	@Test
	void knownPoints_shouldReturnReasonableDistance() {
		Location origin = new Location(0.0, 0.0);
		Location oneDegreeNorth = new Location(1.0, 0.0);

		BigDecimal distance = adapter.calculateKm(origin, oneDegreeNorth);

		assertTrue(distance.compareTo(new BigDecimal("110.0000")) > 0);
		assertTrue(distance.compareTo(new BigDecimal("112.0000")) < 0);
	}

	@Test
	void distance_shouldBeSymmetric() {
		Location a = new Location(-12.0464, -77.0428);
		Location b = new Location(-12.1200, -77.0300);

		BigDecimal ab = adapter.calculateKm(a, b);
		BigDecimal ba = adapter.calculateKm(b, a);

		assertEquals(0, ab.compareTo(ba));
	}
}
