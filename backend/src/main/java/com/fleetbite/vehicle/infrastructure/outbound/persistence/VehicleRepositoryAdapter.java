package com.fleetbite.vehicle.infrastructure.outbound.persistence;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;
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
		VehicleJpaEntity existing = springDataVehicleRepository.findById(vehicle.id().value())
				.orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicle.id().value()));
		vehiclePersistenceMapper.copyToEntity(vehicle, existing);
		VehicleJpaEntity saved = springDataVehicleRepository.save(existing);
		return vehiclePersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Vehicle> findById(VehicleId id) {
		Objects.requireNonNull(id, "id is required");
		return springDataVehicleRepository.findById(id.value())
				.map(vehiclePersistenceMapper::toDomain);
	}

	@Override
	public List<Vehicle> findAll() {
		return springDataVehicleRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt")).stream()
				.map(vehiclePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public void deleteById(VehicleId id) {
		Objects.requireNonNull(id, "id is required");
		if (!springDataVehicleRepository.existsById(id.value())) {
			throw new ResourceNotFoundException("Vehicle", id.value());
		}
		springDataVehicleRepository.deleteById(id.value());
	}

	@Override
	public boolean existsByPlate(String plate) {
		Objects.requireNonNull(plate, "plate is required");
		return springDataVehicleRepository.existsByPlate(plate);
	}

	@Override
	public boolean existsByPlateAndIdNot(String plate, VehicleId id) {
		Objects.requireNonNull(plate, "plate is required");
		Objects.requireNonNull(id, "id is required");
		return springDataVehicleRepository.existsByPlateAndIdNot(plate, id.value());
	}
}
