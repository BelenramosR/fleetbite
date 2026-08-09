package com.fleetbite.delivery.infrastructure.transaction;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.order.domain.model.OrderId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalAutoAssignOrderUseCase implements AutoAssignOrderUseCase {

	private final AutoAssignOrderUseCase delegate;

	public TransactionalAutoAssignOrderUseCase(AutoAssignOrderUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public AutoAssignmentResult execute(OrderId orderId) {
		return delegate.execute(orderId);
	}
}
