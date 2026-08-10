package com.fleetbite.driver.application.port.out;

import java.util.UUID;

import com.fleetbite.driver.domain.model.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverRepositoryPort {

	Driver save(Driver driver);

	Driver update(Driver driver);

	Optional<Driver> findById(UUID id);

	List<Driver> findAll();

	/**
	 * Returns drivers that are AVAILABLE, have a non-null current location, and have a vehicle assigned.
	 */
	List<Driver> findAvailableWithLocation();

	Optional<Driver> findByVehicleId(UUID vehicleId);

	void deleteById(UUID id);

	boolean existsByPhone(String phone);

	boolean existsByPhoneAndIdNot(String phone, UUID id);

	boolean existsByUserId(UUID userId);
}
