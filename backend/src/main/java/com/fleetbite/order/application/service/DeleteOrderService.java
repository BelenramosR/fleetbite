package com.fleetbite.order.application.service;

import com.fleetbite.order.application.port.in.DeleteOrderUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class DeleteOrderService implements DeleteOrderUseCase {

	private final OrderRepositoryPort orderRepositoryPort;

	public DeleteOrderService(OrderRepositoryPort orderRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
	}

	@Override
	public void execute(OrderId orderId) {
		Objects.requireNonNull(orderId, "orderId is required");

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId.value()));

		order.ensureDeletable();
		orderRepositoryPort.deleteById(orderId);
	}
}
