package com.fleetbite.driver.application.port.out;

import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;

import java.util.List;
import java.util.Optional;

public interface DriverRepositoryPort {

	Driver save(Driver driver);

	Driver update(Driver driver);

	Optional<Driver> findById(DriverId id);

	List<Driver> findAll();

	/**
	 * Returns drivers that are AVAILABLE and have a non-null current location.
	 */
	List<Driver> findAvailableWithLocation();

	void deleteById(DriverId id);

	boolean existsByPhone(String phone);

	boolean existsByPhoneAndIdNot(String phone, DriverId id);
}
