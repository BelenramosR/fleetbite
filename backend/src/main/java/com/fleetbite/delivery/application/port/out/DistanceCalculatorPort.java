package com.fleetbite.delivery.application.port.out;

import com.fleetbite.shared.domain.model.Location;

import java.math.BigDecimal;

/**
 * Calculates approximate distance between two geographic points.
 *
 * <p>For auto-assignment, origin is {@code Driver.currentLocation} and destination is the
 * configured restaurant pickup location.
 */
public interface DistanceCalculatorPort {

	BigDecimal calculateKm(Location origin, Location destination);
}
