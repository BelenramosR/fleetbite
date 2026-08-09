package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.dto.UpdateOrderCommand;
import com.fleetbite.order.application.port.in.UpdateOrderUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.model.Location;

import java.util.Objects;

public final class UpdateOrderService implements UpdateOrderUseCase {

	private final OrderRepositoryPort orderRepositoryPort;

	public UpdateOrderService(OrderRepositoryPort orderRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
	}

	@Override
	public OrderResult execute(OrderId orderId, UpdateOrderCommand command) {
		Objects.requireNonNull(orderId, "orderId is required");
		Objects.requireNonNull(command, "command is required");

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId.value()));

		order.updateDetails(
				command.customerName(),
				command.customerPhone(),
				command.deliveryAddress(),
				new Location(command.deliveryLatitude(), command.deliveryLongitude()),
				Money.of(command.totalAmount()));

		Order updated = orderRepositoryPort.update(order);
		return OrderResult.from(updated);
	}
}
