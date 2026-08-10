package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;

import java.util.UUID;

public interface OrderWorkflowUseCase {

	OrderResult confirm(UUID orderId);

	OrderResult startPreparation(UUID orderId);

	OrderResult markReady(UUID orderId);

	OrderResult cancel(UUID orderId, CancelOrderCommand command);
}
