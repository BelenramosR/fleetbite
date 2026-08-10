package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class VehicleRepositoryAdapter implements VehicleRepositoryPort {

	private final SpringDataVehicleRepository springDataVehicleRepository;
	private final VehiclePersistenceMapper vehiclePersistenceMapper;

	public VehicleRepositoryAdapter(
			SpringDataVehicleRepository springDataVehicleRepository,
			VehiclePersistenceMapper vehiclePersistenceMapper) {
		this.springDataVehicleRepository = Objects.requireNonNull(springDataVehicleRepository);
		this.vehiclePersistenceMapper = Objects.requireNonNull(vehiclePersistenceMapper);
	}

	@Override
	public Vehicle save(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		VehicleJpaEntity entity = vehiclePersistenceMapper.toEntity(vehicle);
		VehicleJpaEntity saved = springDataVehicleRepository.save(entity);
		return vehiclePersistenceMapper.toDomain(saved);
	}

	@Override
	public Vehicle update(Vehicle vehicle) {
		Objects.requireNonNull(vehicle, "vehicle is required");
		VehicleJpaEntity existing = springDataVehicleRepository.findById(vehicle.id())
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicle.id()));
		vehiclePersistenceMapper.copyToEntity(vehicle, existing);
		VehicleJpaEntity saved = springDataVehicleRepository.save(existing);
		return vehiclePersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Vehicle> findById(UUID id) {
		Objects.requireNonNull(id, "id is required");
		return springDataVehicleRepository.findById(id)
				.map(vehiclePersistenceMapper::toDomain);
	}

	@Override
	public List<Vehicle> findAll() {
		return springDataVehicleRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt")).stream()
				.map(vehiclePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public void deleteById(UUID id) {
		Objects.requireNonNull(id, "id is required");
		if (!springDataVehicleRepository.existsById(id)) {
			throw new ResourceNotFoundException("Vehicle", id);
		}
		springDataVehicleRepository.deleteById(id);
	}

	@Override
	public boolean existsByPlate(String plate) {
		Objects.requireNonNull(plate, "plate is required");
		return springDataVehicleRepository.existsByPlate(plate);
	}

	@Override
	public boolean existsByPlateAndIdNot(String plate, UUID id) {
		Objects.requireNonNull(plate, "plate is required");
		Objects.requireNonNull(id, "id is required");
		return springDataVehicleRepository.existsByPlateAndIdNot(plate, id);
	}
}
