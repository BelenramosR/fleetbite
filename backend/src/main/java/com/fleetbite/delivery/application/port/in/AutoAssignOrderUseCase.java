package com.fleetbite.delivery.application.port.in;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.order.domain.model.OrderId;

public interface AutoAssignOrderUseCase {

	AutoAssignmentResult execute(OrderId orderId);
}
