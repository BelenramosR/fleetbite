package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderPersistenceMapperTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime PROMISED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 45, 0, 0, BusinessTime.ZONE_OFFSET);

	private final OrderPersistenceMapper mapper = Mappers.getMapper(OrderPersistenceMapper.class);

	@Test
	void toEntity_shouldMapDomainFieldsAndLeaveVersionNull() {
		Order order = createdOrder();

		OrderJpaEntity entity = mapper.toEntity(order);

		assertEquals(order.id().value(), entity.getId());
		assertEquals(order.code().value(), entity.getCode());
		assertEquals(order.customerName(), entity.getCustomerName());
		assertEquals(order.status(), entity.getStatus());
		assertEquals(order.priority(), entity.getPriority());
		assertEquals(order.totalAmount().amount(), entity.getTotalAmount());
		assertEquals(CREATED_AT, entity.getCreatedAt());
		assertEquals(PROMISED_AT, entity.getPromisedDeliveryAt());
		assertNull(entity.getVersion());
		assertNull(entity.getConfirmedAt());
	}

	@Test
	void toDomain_shouldNormalizeOffsetsToBusinessZone() {
		OffsetDateTime createdUtc = OffsetDateTime.of(2026, 8, 9, 3, 0, 0, 0, ZoneOffset.UTC);
		OffsetDateTime confirmedUtc = OffsetDateTime.of(2026, 8, 9, 3, 5, 0, 0, ZoneOffset.UTC);
		OffsetDateTime readyUtc = OffsetDateTime.of(2026, 8, 9, 3, 20, 0, 0, ZoneOffset.UTC);

		OrderJpaEntity entity = new OrderJpaEntity();
		entity.setId(OrderId.generate().value());
		entity.setCode("ORD-2026-ABCDEF12");
		entity.setCustomerName("Ana Torres");
		entity.setCustomerPhone("999999999");
		entity.setDeliveryAddress("Av. Example 123");
		entity.setDeliveryLatitude(new BigDecimal("-12.1001000"));
		entity.setDeliveryLongitude(new BigDecimal("-77.0201000"));
		entity.setTotalAmount(new BigDecimal("85.90"));
		entity.setStatus(OrderStatus.READY);
		entity.setPriority(OrderPriority.HIGH);
		entity.setPromisedDeliveryAt(createdUtc.plusMinutes(45));
		entity.setCreatedAt(createdUtc);
		entity.setConfirmedAt(confirmedUtc);
		entity.setReadyAt(readyUtc);
		entity.setVersion(3L);

		Order order = mapper.toDomain(entity);

		assertEquals(BusinessTime.ZONE_OFFSET, order.createdAt().getOffset());
		assertEquals(BusinessTime.ZONE_OFFSET, order.promisedDeliveryAt().getOffset());
		assertEquals(BusinessTime.ZONE_OFFSET, order.confirmedAt().getOffset());
		assertEquals(BusinessTime.ZONE_OFFSET, order.readyAt().getOffset());
		assertEquals(OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET), order.createdAt());
		assertEquals(OrderStatus.READY, order.status());
		assertEquals(OrderPriority.HIGH, order.priority());
	}

	@Test
	void roundTrip_shouldPreserveCreatedOrderState() {
		Order original = createdOrder();

		Order mapped = mapper.toDomain(mapper.toEntity(original));

		assertEquals(original.id(), mapped.id());
		assertEquals(original.code(), mapped.code());
		assertEquals(original.status(), mapped.status());
		assertEquals(original.createdAt(), mapped.createdAt());
		assertEquals(original.promisedDeliveryAt(), mapped.promisedDeliveryAt());
	}

	private static Order createdOrder() {
		return Order.create(
				OrderId.generate(),
				OrderCode.of("ORD-2026-ABCDEF12"),
				"Ana Torres",
				"999999999",
				"Av. Example 123",
				new Location(-12.1001, -77.0201),
				Money.of(new BigDecimal("85.90")),
				CREATED_AT,
				PROMISED_AT);
	}
}
