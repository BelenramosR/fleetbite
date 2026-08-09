package com.fleetbite.driver.application.port.out;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.vehicle.domain.model.VehicleId;

import java.util.List;
import java.util.Optional;

public interface DriverRepositoryPort {

	Driver save(Driver driver);

	Driver update(Driver driver);

	Optional<Driver> findById(DriverId id);

	List<Driver> findAll();

	/**
	 * Returns drivers that are AVAILABLE, have a non-null current location, and have a vehicle assigned.
	 */
	List<Driver> findAvailableWithLocation();

	Optional<Driver> findByVehicleId(VehicleId vehicleId);

	void deleteById(DriverId id);

	boolean existsByPhone(String phone);

	boolean existsByPhoneAndIdNot(String phone, DriverId id);

	boolean existsByUserId(UserId userId);
}
