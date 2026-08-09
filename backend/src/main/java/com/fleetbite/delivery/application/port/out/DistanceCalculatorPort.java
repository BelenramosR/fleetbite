package com.fleetbite.delivery.application.port.out;

import com.fleetbite.shared.domain.model.Location;

import java.math.BigDecimal;

/**
 * Calculates approximate distance between two geographic points.
 *
 * <p>For auto-assignment MVP, origin is {@code Driver.currentLocation} and destination is
 * {@code Order.deliveryLocation} (distance toward the final delivery point, not restaurant pickup).
 */
public interface DistanceCalculatorPort {

	BigDecimal calculateKm(Location origin, Location destination);
}
