package com.fleetbite.driver.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DriverRepositoryAdapter implements DriverRepositoryPort {

	private final SpringDataDriverRepository springDataDriverRepository;
	private final DriverPersistenceMapper driverPersistenceMapper;

	public DriverRepositoryAdapter(
			SpringDataDriverRepository springDataDriverRepository,
			DriverPersistenceMapper driverPersistenceMapper) {
		this.springDataDriverRepository = Objects.requireNonNull(springDataDriverRepository);
		this.driverPersistenceMapper = Objects.requireNonNull(driverPersistenceMapper);
	}

	@Override
	public Driver save(Driver driver) {
		Objects.requireNonNull(driver, "driver is required");
		DriverJpaEntity entity = driverPersistenceMapper.toEntity(driver);
		DriverJpaEntity saved = springDataDriverRepository.save(entity);
		return driverPersistenceMapper.toDomain(saved);
	}

	@Override
	public Driver update(Driver driver) {
		Objects.requireNonNull(driver, "driver is required");
		DriverJpaEntity existing = springDataDriverRepository.findById(driver.id())
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driver.id()));
		driverPersistenceMapper.copyToEntity(driver, existing);
		DriverJpaEntity saved = springDataDriverRepository.save(existing);
		return driverPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Driver> findById(UUID id) {
		Objects.requireNonNull(id, "id is required");
		return springDataDriverRepository.findById(id)
				.map(driverPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Driver> findByUserId(UUID userId) {
		Objects.requireNonNull(userId, "userId is required");
		return springDataDriverRepository.findByUserId(userId)
				.map(driverPersistenceMapper::toDomain);
	}

	@Override
	public List<Driver> findAll() {
		return springDataDriverRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt")).stream()
				.map(driverPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<Driver> findAvailableWithLocation() {
		return springDataDriverRepository
				.findByStatusAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndVehicleIdIsNotNull(
						DriverStatus.AVAILABLE)
				.stream()
				.map(driverPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public Optional<Driver> findByVehicleId(UUID vehicleId) {
		Objects.requireNonNull(vehicleId, "vehicleId is required");
		return springDataDriverRepository.findByVehicleId(vehicleId)
				.map(driverPersistenceMapper::toDomain);
	}

	@Override
	public void deleteById(UUID id) {
		Objects.requireNonNull(id, "id is required");
		if (!springDataDriverRepository.existsById(id)) {
			throw new ResourceNotFoundException("Driver", id);
		}
		springDataDriverRepository.deleteById(id);
	}

	@Override
	public boolean existsByPhone(String phone) {
		Objects.requireNonNull(phone, "phone is required");
		return springDataDriverRepository.existsByPhone(phone);
	}

	@Override
	public boolean existsByPhoneAndIdNot(String phone, UUID id) {
		Objects.requireNonNull(phone, "phone is required");
		Objects.requireNonNull(id, "id is required");
		return springDataDriverRepository.existsByPhoneAndIdNot(phone, id);
	}

	@Override
	public boolean existsByUserId(UUID userId) {
		Objects.requireNonNull(userId, "userId is required");
		return springDataDriverRepository.existsByUserId(userId);
	}
}
