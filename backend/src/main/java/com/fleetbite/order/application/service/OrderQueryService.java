package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderHistoryResult;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.OrderQueryUseCase;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class OrderQueryService implements OrderQueryUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final OrderHistoryRepositoryPort orderHistoryRepositoryPort;

	public OrderQueryService(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRepositoryPort orderHistoryRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.orderHistoryRepositoryPort = Objects.requireNonNull(orderHistoryRepositoryPort);
	}

	@Override
	public OrderResult getById(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		return orderRepositoryPort.findById(orderId)
				.map(OrderResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
	}

	@Override
	public List<OrderResult> findAll() {
		return orderRepositoryPort.findAll().stream()
				.map(OrderResult::from)
				.toList();
	}

	@Override
	public List<OrderHistoryResult> getHistory(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		if (orderRepositoryPort.findById(orderId).isEmpty()) {
			throw new ResourceNotFoundException("Order", orderId);
		}
		return orderHistoryRepositoryPort.findByOrderId(orderId).stream()
				.map(OrderHistoryResult::from)
				.toList();
	}
}
