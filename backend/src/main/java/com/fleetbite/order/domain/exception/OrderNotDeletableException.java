package com.fleetbite.order.domain.exception;

import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class OrderNotDeletableException extends DomainException {

	public OrderNotDeletableException(OrderStatus status) {
		super(
				"ORDER_NOT_DELETABLE",
				"Order cannot be deleted while in status " + status);
	}
}
