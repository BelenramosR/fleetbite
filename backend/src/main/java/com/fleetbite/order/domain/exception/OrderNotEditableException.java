package com.fleetbite.order.domain.exception;

import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.exception.DomainException;

public final class OrderNotEditableException extends DomainException {

	public OrderNotEditableException(OrderStatus status) {
		super(
				"ORDER_NOT_EDITABLE",
				"Order cannot be edited while in status " + status);
	}
}
