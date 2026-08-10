package com.fleetbite.order.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.order.domain.model.Money;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderCode;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class OrderPersistenceMapper {

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

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "code", expression = "java(order.code().value())")
	@Mapping(target = "customerName", expression = "java(order.customerName())")
	@Mapping(target = "customerPhone", expression = "java(order.customerPhone())")
	@Mapping(target = "deliveryAddress", expression = "java(order.deliveryAddress())")
	@Mapping(target = "deliveryLatitude", expression = "java(toCoordinate(order.deliveryLocation().latitude()))")
	@Mapping(target = "deliveryLongitude", expression = "java(toCoordinate(order.deliveryLocation().longitude()))")
	@Mapping(target = "totalAmount", expression = "java(order.totalAmount().amount())")
	@Mapping(target = "status", expression = "java(order.status())")
	@Mapping(target = "priority", expression = "java(order.priority())")
	@Mapping(target = "promisedDeliveryAt", expression = "java(order.promisedDeliveryAt())")
	@Mapping(target = "createdAt", expression = "java(order.createdAt())")
	@Mapping(target = "confirmedAt", expression = "java(order.confirmedAt())")
	@Mapping(target = "preparationStartedAt", expression = "java(order.preparationStartedAt())")
	@Mapping(target = "readyAt", expression = "java(order.readyAt())")
	@Mapping(target = "assignedAt", expression = "java(order.assignedAt())")
	@Mapping(target = "pickedUpAt", expression = "java(order.pickedUpAt())")
	@Mapping(target = "inTransitAt", expression = "java(order.inTransitAt())")
	@Mapping(target = "deliveredAt", expression = "java(order.deliveredAt())")
	@Mapping(target = "cancelledAt", expression = "java(order.cancelledAt())")
	@Mapping(target = "failedDeliveryAt", expression = "java(order.failedDeliveryAt())")
	protected abstract void copyPersistableState(Order order, @MappingTarget OrderJpaEntity entity);

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

	protected OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}

	protected BigDecimal toCoordinate(double value) {
		return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
	}
}
