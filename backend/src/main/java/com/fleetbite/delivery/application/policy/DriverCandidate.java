package com.fleetbite.delivery.application.policy;

import com.fleetbite.driver.domain.model.Driver;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Candidate produced by {@link DriverSelectionPolicy}.
 *
 * <p>In this phase {@code score} equals {@code distanceKm} (Haversine). A later phase may evolve
 * score into a weighted value (workload + SLA) while keeping distance as a separate signal.
 */
public final class DriverCandidate {

	private final Driver driver;
	private final BigDecimal distanceKm;
	private final BigDecimal score;

	public DriverCandidate(Driver driver, BigDecimal distanceKm, BigDecimal score) {
		this.driver = Objects.requireNonNull(driver, "driver is required");
		this.distanceKm = Objects.requireNonNull(distanceKm, "distanceKm is required");
		this.score = Objects.requireNonNull(score, "score is required");
	}

	public Driver driver() {
		return driver;
	}

	public BigDecimal distanceKm() {
		return distanceKm;
	}

	public BigDecimal score() {
		return score;
	}
}
