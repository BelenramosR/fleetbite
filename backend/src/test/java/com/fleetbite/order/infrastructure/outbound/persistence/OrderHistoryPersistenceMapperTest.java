package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderHistoryPersistenceMapperTest {

	private final OrderHistoryPersistenceMapper mapper = new OrderHistoryPersistenceMapper();

	@Test
	void roundTripPreservesHistoryState() {
		OffsetDateTime createdAt = OffsetDateTime.of(
				2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
		OrderHistoryEvent event = OrderHistoryEvent.record(
				UUID.randomUUID(),
				OrderHistoryEventType.ORDER_CONFIRMED,
				OrderStatus.CREATED,
				OrderStatus.CONFIRMED,
				"confirmed",
				createdAt);

		OrderHistoryEvent mapped = mapper.toDomain(mapper.toEntity(event));

		assertEquals(event.id(), mapped.id());
		assertEquals(event.orderId(), mapped.orderId());
		assertEquals(event.eventType(), mapped.eventType());
		assertEquals(event.previousStatus(), mapped.previousStatus());
		assertEquals(event.newStatus(), mapped.newStatus());
		assertEquals(event.description(), mapped.description());
		assertEquals(createdAt, mapped.createdAt());
	}

	@Test
	void toDomainNormalizesTimestampToBusinessOffset() {
		OrderHistoryJpaEntity entity = new OrderHistoryJpaEntity();
		entity.setId(UUID.randomUUID());
		entity.setOrderId(UUID.randomUUID());
		entity.setEventType(OrderHistoryEventType.ORDER_READY);
		entity.setPreviousStatus(OrderStatus.PREPARING);
		entity.setNewStatus(OrderStatus.READY);
		entity.setCreatedAt(OffsetDateTime.of(2026, 8, 9, 3, 0, 0, 0, ZoneOffset.UTC));

		OrderHistoryEvent mapped = mapper.toDomain(entity);

		assertEquals(BusinessTime.ZONE_OFFSET, mapped.createdAt().getOffset());
		assertEquals(OffsetDateTime.of(
				2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET), mapped.createdAt());
	}
}
