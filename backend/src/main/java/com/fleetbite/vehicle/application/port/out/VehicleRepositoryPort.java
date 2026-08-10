package com.fleetbite.vehicle.application.port.out;

import java.util.UUID;

import com.fleetbite.vehicle.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {

	Vehicle save(Vehicle vehicle);

	Vehicle update(Vehicle vehicle);

	Optional<Vehicle> findById(UUID id);

	List<Vehicle> findAll();

	void deleteById(UUID id);

	boolean existsByPlate(String plate);

	boolean existsByPlateAndIdNot(String plate, UUID id);
}
