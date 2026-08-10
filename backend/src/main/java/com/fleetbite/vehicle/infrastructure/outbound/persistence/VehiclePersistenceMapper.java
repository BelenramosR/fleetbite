package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.domain.model.Vehicle;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class VehiclePersistenceMapper {
	public VehicleJpaEntity toEntity(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		VehicleJpaEntity entity = new VehicleJpaEntity();
		entity.setId(vehicle.id());
		copyToEntity(vehicle, entity);
		return entity;
	}

	public void copyToEntity(Vehicle vehicle, VehicleJpaEntity entity) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		Objects.requireNonNull(entity, "entity is required");
		if (entity.getId() != null && !entity.getId().equals(vehicle.id())) {
			throw new IllegalArgumentException("cannot copy vehicle onto entity with a different id");
		}
		entity.setPlate(vehicle.plate());
		entity.setType(vehicle.type());
		entity.setStatus(vehicle.status());
		entity.setCreatedAt(vehicle.createdAt());
		entity.setUpdatedAt(vehicle.updatedAt());
	}

	public Vehicle toDomain(VehicleJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");
		return Vehicle.reconstitute(entity.getId(), entity.getPlate(), entity.getType(), entity.getStatus(),
				toBusinessOffset(entity.getCreatedAt()), toBusinessOffset(entity.getUpdatedAt()));
	}

	private OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		return value == null ? null : value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
