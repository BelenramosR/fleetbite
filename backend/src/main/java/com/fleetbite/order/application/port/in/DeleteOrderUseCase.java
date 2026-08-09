package com.fleetbite.order.application.port.in;

import com.fleetbite.order.domain.model.OrderId;

public interface DeleteOrderUseCase {

	void execute(OrderId orderId);
}
