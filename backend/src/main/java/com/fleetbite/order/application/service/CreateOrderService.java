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
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public final class CreateOrderService implements CreateOrderUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final Clock clock;

	public CreateOrderService(OrderRepositoryPort orderRepositoryPort, Clock clock) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort, "orderRepositoryPort");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	@Override
	public OrderResult execute(CreateOrderCommand command) {
		Objects.requireNonNull(command, "command is required");

		Instant currentInstant = clock.instant();
		OffsetDateTime createdAt = BusinessTime.toBusinessTime(currentInstant);
		OffsetDateTime promisedDeliveryAt = BusinessTime.defaultPromisedDeliveryAt(createdAt);

		Order order = Order.create(
				OrderId.generate(),
				generateOrderCode(currentInstant),
				command.customerName(),
				command.customerPhone(),
				command.deliveryAddress(),
				new Location(command.deliveryLatitude(), command.deliveryLongitude()),
				Money.of(command.totalAmount()),
				createdAt,
				promisedDeliveryAt);

		Order savedOrder = orderRepositoryPort.save(order);
		return OrderResult.from(savedOrder);
	}

	private static OrderCode generateOrderCode(Instant currentInstant) {
		String year = String.valueOf(LocalDate.ofInstant(currentInstant, BusinessTime.ZONE_OFFSET).getYear());
		String fragment = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
		return OrderCode.of("ORD-" + year + "-" + fragment);
	}
}
