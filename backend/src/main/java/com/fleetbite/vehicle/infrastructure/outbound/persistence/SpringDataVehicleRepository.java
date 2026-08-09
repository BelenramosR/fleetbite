package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataVehicleRepository extends JpaRepository<VehicleJpaEntity, UUID> {

	boolean existsByPlate(String plate);

	boolean existsByPlateAndIdNot(String plate, UUID id);
}
