package com.fleetbite.order.application.port.out;

import com.fleetbite.order.domain.event.OrderReadyEvent;

/**
 * Outbound port for publishing order domain events.
 * Infrastructure adapters may use Spring ApplicationEvents now and EventBridge/SQS later.
 */
public interface DomainEventPublisherPort {

	void publish(OrderReadyEvent event);
}
