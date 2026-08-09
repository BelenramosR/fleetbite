package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;

public interface CreateOrderUseCase {

	OrderResult execute(CreateOrderCommand command);
}
