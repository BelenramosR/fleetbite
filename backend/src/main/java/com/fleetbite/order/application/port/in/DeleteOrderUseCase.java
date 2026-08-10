package com.fleetbite.order.application.port.in;

import java.util.UUID;


public interface DeleteOrderUseCase {

	void execute(UUID orderId);
}
