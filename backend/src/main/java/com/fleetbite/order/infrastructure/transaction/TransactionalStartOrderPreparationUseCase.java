package com.fleetbite.order.infrastructure.transaction;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.StartOrderPreparationUseCase;
import com.fleetbite.order.domain.model.OrderId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalStartOrderPreparationUseCase implements StartOrderPreparationUseCase {

	private final StartOrderPreparationUseCase delegate;

	public TransactionalStartOrderPreparationUseCase(StartOrderPreparationUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public OrderResult execute(OrderId orderId) {
		return delegate.execute(orderId);
	}
}
