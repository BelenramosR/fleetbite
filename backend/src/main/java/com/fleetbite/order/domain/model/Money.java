package com.fleetbite.order.domain.model;

import com.fleetbite.order.domain.exception.InvalidOrderDataException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

	private static final int SCALE = 2;

	private final BigDecimal amount;

	private Money(BigDecimal amount) {
		this.amount = amount;
	}

	public static Money of(BigDecimal amount) {
		if (amount == null) {
			throw new InvalidOrderDataException("totalAmount is required");
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidOrderDataException("totalAmount must be greater than or equal to zero");
		}
		return new Money(amount.setScale(SCALE, RoundingMode.HALF_UP));
	}

	public BigDecimal amount() {
		return amount;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Money that)) {
			return false;
		}
		return amount.compareTo(that.amount) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount.stripTrailingZeros());
	}

	@Override
	public String toString() {
		return amount.toPlainString();
	}
}
