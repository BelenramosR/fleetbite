package com.fleetbite.shared.domain.time;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Business timezone helpers. Does not read the system clock.
 */
public final class BusinessTime {

	public static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofHours(-5);

	public static final Duration STANDARD_DELIVERY_PROMISE = Duration.ofMinutes(45);

	private BusinessTime() {
	}

	public static OffsetDateTime toBusinessTime(Instant instant) {
		Objects.requireNonNull(instant, "instant is required");
		return OffsetDateTime.ofInstant(instant, ZONE_OFFSET);
	}

	public static OffsetDateTime defaultPromisedDeliveryAt(OffsetDateTime createdAt) {
		Objects.requireNonNull(createdAt, "createdAt is required");
		return createdAt.plus(STANDARD_DELIVERY_PROMISE);
	}
}
