package com.fleetbite.order.domain.exception;

import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class InvalidOrderTransitionException extends DomainException {

	public InvalidOrderTransitionException(OrderStatus from, OrderStatus to) {
		super(
				"INVALID_ORDER_TRANSITION",
				"The order cannot transition from " + from + " to " + to);
	}
}
