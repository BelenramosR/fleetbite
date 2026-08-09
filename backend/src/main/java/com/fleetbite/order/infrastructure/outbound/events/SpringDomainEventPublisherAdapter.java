package com.fleetbite.order.infrastructure.outbound.events;

import com.fleetbite.order.application.port.out.DomainEventPublisherPort;
import com.fleetbite.order.domain.event.OrderReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SpringDomainEventPublisherAdapter implements DomainEventPublisherPort {

	private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisherAdapter.class);

	private final ApplicationEventPublisher applicationEventPublisher;

	public SpringDomainEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
	}

	@Override
	public void publish(OrderReadyEvent event) {
		Objects.requireNonNull(event, "event is required");
		applicationEventPublisher.publishEvent(event);
		log.info(
				"ORDER_READY published eventId={} eventType=ORDER_READY orderId={}",
				event.eventId(),
				event.orderId().value());
	}
}
