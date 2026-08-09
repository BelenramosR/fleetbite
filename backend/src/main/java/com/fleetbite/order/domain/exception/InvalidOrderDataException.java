package com.fleetbite.order.domain.exception;

import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidOrderDataException extends DomainException {

	public InvalidOrderDataException(String message) {
		super("INVALID_ORDER_DATA", message);
	}
}
