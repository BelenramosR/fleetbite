package com.fleetbite.order.application.port.out;

import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderId;

import java.util.List;

public interface OrderHistoryRepositoryPort {

	void save(OrderHistoryEvent event);

	List<OrderHistoryEvent> findByOrderId(OrderId orderId);
}
