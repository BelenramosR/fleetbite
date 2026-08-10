package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.domain.model.Driver;
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
		entity.setId(driver.id());
		copyToEntity(driver, entity);
		return entity;
	}

	public void copyToEntity(Driver driver, DriverJpaEntity entity) {
		Objects.requireNonNull(driver, "driver is required");
		Objects.requireNonNull(entity, "entity is required");
		if (entity.getId() != null && !entity.getId().equals(driver.id())) {
			throw new IllegalArgumentException("cannot copy driver onto entity with a different id");
		}
		entity.setUserId(driver.userId());
		entity.setPhone(driver.phone());
		entity.setStatus(driver.status());
		entity.setCurrentLatitude(driver.currentLocation() == null ? null : coordinate(driver.currentLocation().latitude()));
		entity.setCurrentLongitude(driver.currentLocation() == null ? null : coordinate(driver.currentLocation().longitude()));
		entity.setVehicleId(driver.vehicleId());
		entity.setCreatedAt(driver.createdAt());
		entity.setUpdatedAt(driver.updatedAt());
	}

	public Driver toDomain(DriverJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		Location location = entity.getCurrentLatitude() == null || entity.getCurrentLongitude() == null
				? null
				: new Location(entity.getCurrentLatitude().doubleValue(), entity.getCurrentLongitude().doubleValue());
		return Driver.reconstitute(entity.getId(), entity.getUserId(), entity.getPhone(), entity.getStatus(),
				location, entity.getVehicleId(), toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getUpdatedAt()));
	}

	private BigDecimal coordinate(double value) {
		return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
	}

	private OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		return value == null ? null : value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
