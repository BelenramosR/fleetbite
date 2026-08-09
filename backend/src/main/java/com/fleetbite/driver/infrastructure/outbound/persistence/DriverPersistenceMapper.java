package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.domain.model.VehicleId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class DriverPersistenceMapper {

	private static final int COORDINATE_SCALE = 7;

	public DriverJpaEntity toEntity(Driver driver) {
		Objects.requireNonNull(driver, "driver is required");

		DriverJpaEntity entity = new DriverJpaEntity();
		entity.setId(driver.id().value());
		copyPersistableState(driver, entity);
		return entity;
	}

	public void copyToEntity(Driver driver, DriverJpaEntity existingEntity) {
		Objects.requireNonNull(driver, "driver is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(driver.id().value())) {
			throw new IllegalArgumentException("cannot copy driver onto entity with a different id");
		}
		copyPersistableState(driver, existingEntity);
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "userId", expression = "java(driver.userId().value())")
	@Mapping(target = "phone", expression = "java(driver.phone())")
	@Mapping(target = "status", expression = "java(driver.status())")
	@Mapping(
			target = "currentLatitude",
			expression = "java(driver.currentLocation() == null ? null : toCoordinate(driver.currentLocation().latitude()))")
	@Mapping(
			target = "currentLongitude",
			expression = "java(driver.currentLocation() == null ? null : toCoordinate(driver.currentLocation().longitude()))")
	@Mapping(
			target = "vehicleId",
			expression = "java(driver.vehicleId() == null ? null : driver.vehicleId().value())")
	@Mapping(target = "createdAt", expression = "java(driver.createdAt())")
	@Mapping(target = "updatedAt", expression = "java(driver.updatedAt())")
	protected abstract void copyPersistableState(Driver driver, @MappingTarget DriverJpaEntity entity);

	public Driver toDomain(DriverJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");

		Location location = null;
		if (entity.getCurrentLatitude() != null && entity.getCurrentLongitude() != null) {
			location = new Location(
					entity.getCurrentLatitude().doubleValue(),
					entity.getCurrentLongitude().doubleValue());
		}

		VehicleId vehicleId = entity.getVehicleId() == null ? null : VehicleId.of(entity.getVehicleId());

		return Driver.reconstitute(
				DriverId.of(entity.getId()),
				UserId.of(entity.getUserId()),
				entity.getPhone(),
				entity.getStatus(),
				location,
				vehicleId,
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getUpdatedAt()));
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
