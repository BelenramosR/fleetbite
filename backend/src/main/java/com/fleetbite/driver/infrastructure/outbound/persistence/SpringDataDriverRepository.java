package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.domain.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataDriverRepository extends JpaRepository<DriverJpaEntity, UUID> {

	boolean existsByPhone(String phone);

	boolean existsByPhoneAndIdNot(String phone, UUID id);

	boolean existsByUserId(UUID userId);

	Optional<DriverJpaEntity> findByUserId(UUID userId);

	Optional<DriverJpaEntity> findByVehicleId(UUID vehicleId);

	List<DriverJpaEntity> findByStatusAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndVehicleIdIsNotNull(
			DriverStatus status);
}
