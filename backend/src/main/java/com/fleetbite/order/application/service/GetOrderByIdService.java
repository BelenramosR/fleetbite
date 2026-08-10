package com.fleetbite.order.application.service;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.GetOrderByIdUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class GetOrderByIdService implements GetOrderByIdUseCase {

	private final OrderRepositoryPort orderRepositoryPort;

	public GetOrderByIdService(OrderRepositoryPort orderRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort, "orderRepositoryPort");
	}

	@Override
	public OrderResult execute(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");

		return orderRepositoryPort.findById(orderId)
				.map(OrderResult::from)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
	}
}
