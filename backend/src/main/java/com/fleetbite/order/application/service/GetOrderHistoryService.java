package com.fleetbite.order.application.service;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderHistoryResult;
import com.fleetbite.order.application.port.in.GetOrderHistoryUseCase;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;

public final class GetOrderHistoryService implements GetOrderHistoryUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final OrderHistoryRepositoryPort orderHistoryRepositoryPort;

	public GetOrderHistoryService(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRepositoryPort orderHistoryRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.orderHistoryRepositoryPort = Objects.requireNonNull(orderHistoryRepositoryPort);
	}

	@Override
	public List<OrderHistoryResult> execute(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		if (orderRepositoryPort.findById(orderId).isEmpty()) {
			throw new ResourceNotFoundException("Order", orderId);
		}
		return orderHistoryRepositoryPort.findByOrderId(orderId).stream()
				.map(OrderHistoryResult::from)
				.toList();
	}
}
