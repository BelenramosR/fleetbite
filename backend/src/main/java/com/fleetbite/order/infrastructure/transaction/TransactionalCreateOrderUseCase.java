package com.fleetbite.order.infrastructure.transaction;

import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalCreateOrderUseCase implements CreateOrderUseCase {

	private final CreateOrderUseCase delegate;

	public TransactionalCreateOrderUseCase(CreateOrderUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public OrderResult execute(CreateOrderCommand command) {
		return delegate.execute(command);
	}
}
