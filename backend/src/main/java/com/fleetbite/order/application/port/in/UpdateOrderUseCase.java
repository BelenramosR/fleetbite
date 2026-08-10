package com.fleetbite.order.application.port.in;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.dto.UpdateOrderCommand;

public interface UpdateOrderUseCase {

	OrderResult execute(UUID orderId, UpdateOrderCommand command);
}
