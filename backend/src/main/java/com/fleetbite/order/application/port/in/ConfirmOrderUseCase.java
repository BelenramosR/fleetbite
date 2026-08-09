package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.domain.model.OrderId;

public interface ConfirmOrderUseCase {

	OrderResult execute(OrderId orderId);
}
