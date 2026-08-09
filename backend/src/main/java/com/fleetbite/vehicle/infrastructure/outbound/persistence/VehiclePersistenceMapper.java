package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
public class VehiclePersistenceMapper {

	public VehicleJpaEntity toEntity(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");

		VehicleJpaEntity entity = new VehicleJpaEntity();
		entity.setId(vehicle.id().value());
		copyPersistableState(vehicle, entity);
		return entity;
	}

	public void copyToEntity(Vehicle vehicle, VehicleJpaEntity existingEntity) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(vehicle.id().value())) {
			throw new IllegalArgumentException("cannot copy vehicle onto entity with a different id");
		}
		copyPersistableState(vehicle, existingEntity);
	}

	private void copyPersistableState(Vehicle vehicle, VehicleJpaEntity entity) {
		entity.setPlate(vehicle.plate());
		entity.setType(vehicle.type());
		entity.setStatus(vehicle.status());
		entity.setCreatedAt(vehicle.createdAt());
		entity.setUpdatedAt(vehicle.updatedAt());
	}

	public Vehicle toDomain(VehicleJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");

		return Vehicle.reconstitute(
				VehicleId.of(entity.getId()),
				entity.getPlate(),
				entity.getType(),
				entity.getStatus(),
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getUpdatedAt()));
	}

	private static OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
