package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.dto.UpdateOrderCommand;
import com.fleetbite.order.domain.model.OrderId;

public interface UpdateOrderUseCase {

	OrderResult execute(OrderId orderId, UpdateOrderCommand command);
}
