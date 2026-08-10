package com.fleetbite.delivery.application.policy;

import com.fleetbite.delivery.application.port.out.DistanceCalculatorPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.shared.domain.model.Location;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Deterministic nearest-driver selection using Haversine distance to {@link Order#deliveryLocation()}.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Keep only AVAILABLE drivers with a non-null current location</li>
 *   <li>Compute distanceKm from driver location to order delivery location</li>
 *   <li>Pick the smallest distanceKm</li>
 *   <li>On tie, pick the smallest driver UUID</li>
 * </ol>
 *
 * <p>MVP score = distanceKm (temporary). Later phases may introduce weighted scores.
 */
public final class NearestDriverSelectionPolicy implements DriverSelectionPolicy {

	private final DistanceCalculatorPort distanceCalculatorPort;

	public NearestDriverSelectionPolicy(DistanceCalculatorPort distanceCalculatorPort) {
		this.distanceCalculatorPort = Objects.requireNonNull(distanceCalculatorPort);
	}

	@Override
	public Optional<DriverCandidate> select(Order order, List<Driver> availableDrivers) {
		Objects.requireNonNull(order, "order is required");
		Objects.requireNonNull(availableDrivers, "availableDrivers is required");

		Location destination = order.deliveryLocation();
		if (destination == null) {
			return Optional.empty();
		}

		return availableDrivers.stream()
				.filter(driver -> driver.status() == DriverStatus.AVAILABLE)
				.filter(driver -> driver.currentLocation() != null)
				.map(driver -> {
					BigDecimal distanceKm = distanceCalculatorPort.calculateKm(
							driver.currentLocation(),
							destination);
					return new DriverCandidate(driver, distanceKm, distanceKm);
				})
				.min(Comparator
						.comparing(DriverCandidate::distanceKm)
						.thenComparing(candidate -> candidate.driver().id()));
	}
}
