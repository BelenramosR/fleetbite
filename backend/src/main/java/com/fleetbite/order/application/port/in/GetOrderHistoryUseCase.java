package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.OrderHistoryResult;
import com.fleetbite.order.domain.model.OrderId;

import java.util.List;

public interface GetOrderHistoryUseCase {

	List<OrderHistoryResult> execute(OrderId orderId);
}
