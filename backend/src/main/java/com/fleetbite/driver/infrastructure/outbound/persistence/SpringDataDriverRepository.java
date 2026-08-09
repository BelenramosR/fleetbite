package com.fleetbite.driver.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataDriverRepository extends JpaRepository<DriverJpaEntity, UUID> {

	boolean existsByPhone(String phone);

	boolean existsByPhoneAndIdNot(String phone, UUID id);
}
