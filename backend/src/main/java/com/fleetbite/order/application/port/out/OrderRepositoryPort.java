package com.fleetbite.order.application.port.out;

import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;

import java.util.Optional;

public interface OrderRepositoryPort {

	Order save(Order order);

	Optional<Order> findById(OrderId id);
}
