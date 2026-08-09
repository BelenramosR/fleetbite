package com.fleetbite.order.application.service;

import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.model.Location;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class CreateOrderService implements CreateOrderUseCase {

	private final OrderRepositoryPort orderRepositoryPort;

	public CreateOrderService(OrderRepositoryPort orderRepositoryPort) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort, "orderRepositoryPort");
	}

	@Override
	public OrderResult execute(CreateOrderCommand command) {
		Objects.requireNonNull(command, "command is required");

		Order order = Order.create(
				OrderId.generate(),
				generateOrderCode(),
				command.customerName(),
				command.customerPhone(),
				command.deliveryAddress(),
				new Location(command.deliveryLatitude(), command.deliveryLongitude()),
				Money.of(command.totalAmount()),
				command.promisedDeliveryAt());

		Order savedOrder = orderRepositoryPort.save(order);
		return OrderResult.from(savedOrder);
	}

	private static OrderCode generateOrderCode() {
		String year = String.valueOf(LocalDate.now().getYear());
		String fragment = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
		return OrderCode.of("ORD-" + year + "-" + fragment);
	}
}
