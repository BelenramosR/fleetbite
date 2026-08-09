package com.fleetbite.order.infrastructure.inbound.events;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class OrderReadyEventListener {

	private static final Logger log = LoggerFactory.getLogger(OrderReadyEventListener.class);

	private final AutoAssignOrderUseCase autoAssignOrderUseCase;

	public OrderReadyEventListener(AutoAssignOrderUseCase autoAssignOrderUseCase) {
		this.autoAssignOrderUseCase = Objects.requireNonNull(autoAssignOrderUseCase);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onOrderReady(OrderReadyEvent event) {
		Objects.requireNonNull(event, "event is required");
		log.info(
				"ORDER_READY processing started eventId={} eventType=ORDER_READY orderId={}",
				event.eventId(),
				event.orderId().value());
		try {
			AutoAssignmentResult result = autoAssignOrderUseCase.execute(event.orderId());
			if (result.assigned()) {
				log.info(
						"ORDER_READY processed: ASSIGNED eventId={} eventType=ORDER_READY orderId={}",
						event.eventId(),
						event.orderId().value());
			}
			else {
				log.info(
						"ORDER_READY processed: WAITING_FOR_DRIVER eventId={} eventType=ORDER_READY orderId={}",
						event.eventId(),
						event.orderId().value());
			}
		}
		catch (RuntimeException ex) {
			log.error(
					"ORDER_READY processing failed eventId={} eventType=ORDER_READY orderId={}",
					event.eventId(),
					event.orderId().value(),
					ex);
		}
	}
}
