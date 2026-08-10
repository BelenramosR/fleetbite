package com.fleetbite.delivery.infrastructure.inbound.events;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

@Component
public class OrderReadyEventListener {

	private static final Logger log = LoggerFactory.getLogger(OrderReadyEventListener.class);

	private final AutoAssignOrderUseCase autoAssignOrderUseCase;
	private final TransactionTemplate requiresNewTransactionTemplate;

	public OrderReadyEventListener(
			AutoAssignOrderUseCase autoAssignOrderUseCase,
			PlatformTransactionManager transactionManager) {
		this.autoAssignOrderUseCase = Objects.requireNonNull(autoAssignOrderUseCase);
		Objects.requireNonNull(transactionManager, "transactionManager is required");
		this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
		this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onOrderReady(OrderReadyEvent event) {
		Objects.requireNonNull(event, "event is required");
		log.info(
				"ORDER_READY processing started eventId={} eventType=ORDER_READY orderId={}",
				event.eventId(),
				event.orderId());
		try {
			AutoAssignmentResult result = requiresNewTransactionTemplate.execute(
					status -> autoAssignOrderUseCase.execute(event.orderId()));
			if (result != null && result.assigned()) {
				log.info(
						"ORDER_READY processed: ASSIGNED eventId={} eventType=ORDER_READY orderId={}",
						event.eventId(),
						event.orderId());
			}
			else {
				log.info(
						"ORDER_READY processed: WAITING_FOR_DRIVER eventId={} eventType=ORDER_READY orderId={}",
						event.eventId(),
						event.orderId());
			}
		}
		catch (RuntimeException ex) {
			log.error(
					"ORDER_READY processing failed eventId={} eventType=ORDER_READY orderId={}",
					event.eventId(),
					event.orderId(),
					ex);
		}
	}
}
