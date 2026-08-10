package com.fleetbite.order.application.port.in;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderHistoryResult;

import java.util.List;

public interface GetOrderHistoryUseCase {

	List<OrderHistoryResult> execute(UUID orderId);
}
