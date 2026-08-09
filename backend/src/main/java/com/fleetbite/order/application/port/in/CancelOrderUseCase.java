package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.domain.model.OrderId;

public interface CancelOrderUseCase {

	OrderResult execute(OrderId orderId, CancelOrderCommand command);
}
