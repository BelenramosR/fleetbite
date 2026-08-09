package com.fleetbite.order.infrastructure.inbound.events;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReadyEventListenerTest {

	@Mock
	private AutoAssignOrderUseCase autoAssignOrderUseCase;
	@Mock
	private PlatformTransactionManager transactionManager;

	private OrderReadyEventListener listener;

	@BeforeEach
	void setUp() {
		when(transactionManager.getTransaction(any(TransactionDefinition.class)))
				.thenReturn(new SimpleTransactionStatus());
		listener = new OrderReadyEventListener(autoAssignOrderUseCase, transactionManager);
	}

	@Test
	void onOrderReady_shouldDelegateToAutoAssignWithSameOrderId() {
		OrderId orderId = OrderId.of(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
		OrderReadyEvent event = OrderReadyEvent.of(
				orderId,
				OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET));
		when(autoAssignOrderUseCase.execute(orderId))
				.thenReturn(AutoAssignmentResult.assigned(
						orderId.value(),
						UUID.randomUUID(),
						UUID.randomUUID(),
						null,
						null));

		listener.onOrderReady(event);

		verify(autoAssignOrderUseCase).execute(eq(orderId));
		verify(transactionManager).commit(any(TransactionStatus.class));
	}

	@Test
	void onOrderReady_shouldNotRethrowWhenAutoAssignFails() {
		OrderId orderId = OrderId.of(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		OrderReadyEvent event = OrderReadyEvent.of(
				orderId,
				OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET));
		when(autoAssignOrderUseCase.execute(orderId))
				.thenThrow(new ActiveAssignmentAlreadyExistsException(orderId.value()));

		assertDoesNotThrow(() -> listener.onOrderReady(event));
		verify(autoAssignOrderUseCase).execute(eq(orderId));
	}
}
