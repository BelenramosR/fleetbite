package com.fleetbite.order.infrastructure.transaction;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.ConfirmOrderUseCase;
import com.fleetbite.order.domain.model.OrderId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalConfirmOrderUseCase implements ConfirmOrderUseCase {

	private final ConfirmOrderUseCase delegate;

	public TransactionalConfirmOrderUseCase(ConfirmOrderUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public OrderResult execute(OrderId orderId) {
		return delegate.execute(orderId);
	}
}
