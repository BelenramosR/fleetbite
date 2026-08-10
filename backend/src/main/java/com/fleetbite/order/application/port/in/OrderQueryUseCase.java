package com.fleetbite.order.application.port.in;

import com.fleetbite.order.application.dto.OrderHistoryResult;
import com.fleetbite.order.application.dto.OrderResult;

import java.util.List;
import java.util.UUID;

public interface OrderQueryUseCase {

	OrderResult getById(UUID orderId);

	List<OrderResult> findAll();

	List<OrderHistoryResult> getHistory(UUID orderId);
}
