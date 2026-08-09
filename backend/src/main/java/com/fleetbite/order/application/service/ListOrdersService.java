package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.ListOrdersUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;

import java.util.List;
import java.util.Objects;

public final class ListOrdersService implements ListOrdersUseCase {

	private final OrderRepositoryPort orderRepositoryPort;

	public ListOrdersService(OrderRepositoryPort orderRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
	}

	@Override
	public List<OrderResult> execute() {
		return orderRepositoryPort.findAll().stream()
				.map(OrderResult::from)
				.toList();
	}
}
