package com.fleetbite.vehicle.application.port.out;

import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {

	Vehicle save(Vehicle vehicle);

	Vehicle update(Vehicle vehicle);

	Optional<Vehicle> findById(VehicleId id);

	List<Vehicle> findAll();

	void deleteById(VehicleId id);

	boolean existsByPlate(String plate);

	boolean existsByPlateAndIdNot(String plate, VehicleId id);
}
