package com.fleetbite.order.application.port.in;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderResult;

public interface ConfirmOrderUseCase {

	OrderResult execute(UUID orderId);
}
