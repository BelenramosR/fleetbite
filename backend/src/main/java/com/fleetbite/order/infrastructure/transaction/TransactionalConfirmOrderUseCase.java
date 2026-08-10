package com.fleetbite.order.infrastructure.transaction;

import java.util.UUID;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.ConfirmOrderUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalConfirmOrderUseCase implements ConfirmOrderUseCase {

	private final ConfirmOrderUseCase delegate;

	public TransactionalConfirmOrderUseCase(ConfirmOrderUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public OrderResult execute(UUID orderId) {
		return delegate.execute(orderId);
	}
}
