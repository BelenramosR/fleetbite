package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.domain.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.OffsetDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class VehiclePersistenceMapper {

	public VehicleJpaEntity toEntity(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");

		VehicleJpaEntity entity = new VehicleJpaEntity();
		entity.setId(vehicle.id());
		copyPersistableState(vehicle, entity);
		return entity;
	}

	public void copyToEntity(Vehicle vehicle, VehicleJpaEntity existingEntity) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		Objects.requireNonNull(existingEntity, "existingEntity is required");
		if (!existingEntity.getId().equals(vehicle.id())) {
			throw new IllegalArgumentException("cannot copy vehicle onto entity with a different id");
		}
		copyPersistableState(vehicle, existingEntity);
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "plate", expression = "java(vehicle.plate())")
	@Mapping(target = "type", expression = "java(vehicle.type())")
	@Mapping(target = "status", expression = "java(vehicle.status())")
	@Mapping(target = "createdAt", expression = "java(vehicle.createdAt())")
	@Mapping(target = "updatedAt", expression = "java(vehicle.updatedAt())")
	protected abstract void copyPersistableState(Vehicle vehicle, @MappingTarget VehicleJpaEntity entity);

	public Vehicle toDomain(VehicleJpaEntity entity) {
		Objects.requireNonNull(entity, "entity is required");

		return Vehicle.reconstitute(
				entity.getId(),
				entity.getPlate(),
				entity.getType(),
				entity.getStatus(),
				toBusinessOffset(entity.getCreatedAt()),
				toBusinessOffset(entity.getUpdatedAt()));
	}

	protected OffsetDateTime toBusinessOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(BusinessTime.ZONE_OFFSET);
	}
}
