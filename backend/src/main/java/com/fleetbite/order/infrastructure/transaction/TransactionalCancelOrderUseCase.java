package com.fleetbite.order.infrastructure.transaction;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.CancelOrderUseCase;
import com.fleetbite.order.domain.model.OrderId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalCancelOrderUseCase implements CancelOrderUseCase {

	private final CancelOrderUseCase delegate;

	public TransactionalCancelOrderUseCase(CancelOrderUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public OrderResult execute(OrderId orderId, CancelOrderCommand command) {
		return delegate.execute(orderId, command);
	}
}
