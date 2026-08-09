package com.fleetbite.delivery.infrastructure.outbound.geo;

import com.fleetbite.delivery.application.port.out.DistanceCalculatorPort;
import com.fleetbite.shared.domain.model.Location;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Haversine great-circle distance in kilometres (no external map providers).
 */
@Component
public class HaversineDistanceAdapter implements DistanceCalculatorPort {

	private static final double EARTH_RADIUS_KM = 6371.0;
	private static final int SCALE = 4;
	private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

	@Override
	public BigDecimal calculateKm(Location origin, Location destination) {
		Objects.requireNonNull(origin, "origin is required");
		Objects.requireNonNull(destination, "destination is required");

		double lat1 = Math.toRadians(origin.latitude());
		double lat2 = Math.toRadians(destination.latitude());
		double deltaLat = Math.toRadians(destination.latitude() - origin.latitude());
		double deltaLon = Math.toRadians(destination.longitude() - origin.longitude());

		double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
				+ Math.cos(lat1) * Math.cos(lat2)
				* Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distanceKm = EARTH_RADIUS_KM * c;

		return new BigDecimal(distanceKm, MATH_CONTEXT).setScale(SCALE, RoundingMode.HALF_UP);
	}
}
