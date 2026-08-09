package com.fleetbite.shared.domain.model;

import java.util.Objects;

/**
 * Geographic coordinates shared by orders, drivers and distance calculations.
 */
public final class Location {

	private final double latitude;
	private final double longitude;

	public Location(double latitude, double longitude) {
		if (latitude < -90 || latitude > 90) {
			throw new IllegalArgumentException("latitude must be between -90 and 90");
		}
		if (longitude < -180 || longitude > 180) {
			throw new IllegalArgumentException("longitude must be between -180 and 180");
		}
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double latitude() {
		return latitude;
	}

	public double longitude() {
		return longitude;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Location that)) {
			return false;
		}
		return Double.compare(latitude, that.latitude) == 0
				&& Double.compare(longitude, that.longitude) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude);
	}

	@Override
	public String toString() {
		return "Location[latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}
