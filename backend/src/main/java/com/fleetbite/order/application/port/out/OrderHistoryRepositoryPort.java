package com.fleetbite.order.application.port.out;

import java.util.UUID;

import com.fleetbite.order.domain.model.OrderHistoryEvent;

import java.util.List;

public interface OrderHistoryRepositoryPort {

	void save(OrderHistoryEvent event);

	List<OrderHistoryEvent> findByOrderId(UUID orderId);
}
