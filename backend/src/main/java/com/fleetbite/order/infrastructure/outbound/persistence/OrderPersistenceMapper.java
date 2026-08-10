package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class OrderPersistenceMapper {

	private static final int COORDINATE_SCALE = 7;

	public OrderJpaEntity toEntity(Order order) {
		Objects.requireNonNull(order, "order is required");

		OrderJpaEntity entity = new OrderJpaEntity();
		entity.setId(order.id());
		copyPersistableState(order, entity);
		// version remains null so Spring Data treats this as a new entity (CREATE)
		return entity;
	}

	/**
	 * Copies domain state onto an existing JPA entity while preserving id and {@code @Version}.
	 * Used exclusively for UPDATE so optimistic locking keeps working.
	 */
	public void copyToEntity(Order order, OrderJpaEntity existingEntity) {
		Objects.requireNonNull(order, "order is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(order.id())) {
			throw new IllegalArgumentException("cannot copy order onto entity with a different id");
		}
		copyPersistableState(order, existingEntity);
	}

	private void copyPersistableState(Order order, OrderJpaEntity entity) {
		entity.setCode(order.code().value());
		entity.setCustomerName(order.customerName());
		entity.setCustomerPhone(order.customerPhone());
		entity.setDeliveryAddress(order.deliveryAddress());
		entity.setDeliveryLatitude(toCoordinate(order.deliveryLocation().latitude()));
		entity.setDeliveryLongitude(toCoordinate(order.deliveryLocation().longitude()));
		entity.setTotalAmount(order.totalAmount().amount());
		entity.setStatus(order.status());
		entity.setPriority(order.priority());
		entity.setPromisedDeliveryAt(order.promisedDeliveryAt());
		entity.setCreatedAt(order.createdAt());
		entity.setConfirmedAt(order.confirmedAt());
		entity.setPreparationStartedAt(order.preparationStartedAt());
		entity.setReadyAt(order.readyAt());
		entity.setAssignedAt(order.assignedAt());
		entity.setPickedUpAt(order.pickedUpAt());
		entity.setInTransitAt(order.inTransitAt());
		entity.setDeliveredAt(order.deliveredAt());
		entity.setCancelledAt(order.cancelledAt());
		entity.setFailedDeliveryAt(order.failedDeliveryAt());
	}

	public Order toDomain(OrderJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");

		return Order.reconstitute(
				entity.getId(),
				OrderCode.of(entity.getCode()),
				entity.getCustomerName(),
				entity.getCustomerPhone(),
				entity.getDeliveryAddress(),
				new Location(
						entity.getDeliveryLatitude().doubleValue(),
						entity.getDeliveryLongitude().doubleValue()),
				Money.of(entity.getTotalAmount()),
				entity.getPriority(),
				entity.getStatus(),
				toBusinessOffset(entity.getPromisedDeliveryAt()),
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getConfirmedAt()),
				toBusinessOffset(entity.getPreparationStartedAt()),
				toBusinessOffset(entity.getReadyAt()),
				toBusinessOffset(entity.getAssignedAt()),
				toBusinessOffset(entity.getPickedUpAt()),
				toBusinessOffset(entity.getInTransitAt()),
				toBusinessOffset(entity.getDeliveredAt()),
				toBusinessOffset(entity.getCancelledAt()),
				toBusinessOffset(entity.getFailedDeliveryAt()));
	}

	private OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}

	private BigDecimal toCoordinate(double value) {
		return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
	}
}
