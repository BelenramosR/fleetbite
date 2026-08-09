package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class DriverPersistenceMapper {

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

	private void copyPersistableState(Driver driver, DriverJpaEntity entity) {
		entity.setName(driver.name());
		entity.setPhone(driver.phone());
		entity.setStatus(driver.status());
		if (driver.currentLocation() == null) {
			entity.setCurrentLatitude(null);
			entity.setCurrentLongitude(null);
		}
		else {
			entity.setCurrentLatitude(toCoordinate(driver.currentLocation().latitude()));
			entity.setCurrentLongitude(toCoordinate(driver.currentLocation().longitude()));
		}
		entity.setCreatedAt(driver.createdAt());
		entity.setUpdatedAt(driver.updatedAt());
	}

	public Driver toDomain(DriverJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");

		Location location = null;
		if (entity.getCurrentLatitude() != null && entity.getCurrentLongitude() != null) {
			location = new Location(
					entity.getCurrentLatitude().doubleValue(),
					entity.getCurrentLongitude().doubleValue());
		}

		return Driver.reconstitute(
				DriverId.of(entity.getId()),
				entity.getName(),
				entity.getPhone(),
				entity.getStatus(),
				location,
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getUpdatedAt()));
	}

	private static OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}

	private static BigDecimal toCoordinate(double value) {
		return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
	}
}
