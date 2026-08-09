package com.fleetbite.delivery.domain.exception;

import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class OrderNotAssignableException extends DomainException {

	public OrderNotAssignableException(OrderStatus status) {
		super(
				"ORDER_NOT_ASSIGNABLE",
				"Order cannot be assigned while in status " + status);
	}
}
