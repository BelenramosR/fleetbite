package com.fleetbite.delivery.infrastructure.outbound.persistence;

import java.util.UUID;

import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.model.AssignmentStatus;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class DeliveryAssignmentRepositoryAdapter implements DeliveryAssignmentRepositoryPort {

	private static final Set<AssignmentStatus> ACTIVE_STATUSES =
			EnumSet.of(AssignmentStatus.PENDING, AssignmentStatus.ACCEPTED);

	private final SpringDataDeliveryAssignmentRepository springDataDeliveryAssignmentRepository;
	private final DeliveryAssignmentPersistenceMapper deliveryAssignmentPersistenceMapper;

	public DeliveryAssignmentRepositoryAdapter(
			SpringDataDeliveryAssignmentRepository springDataDeliveryAssignmentRepository,
			DeliveryAssignmentPersistenceMapper deliveryAssignmentPersistenceMapper) {
		this.springDataDeliveryAssignmentRepository =
				Objects.requireNonNull(springDataDeliveryAssignmentRepository);
		this.deliveryAssignmentPersistenceMapper = Objects.requireNonNull(deliveryAssignmentPersistenceMapper);
	}

	@Override
	public DeliveryAssignment save(DeliveryAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment is required");
		DeliveryAssignmentJpaEntity entity = deliveryAssignmentPersistenceMapper.toEntity(assignment);
		DeliveryAssignmentJpaEntity saved = springDataDeliveryAssignmentRepository.save(entity);
		return deliveryAssignmentPersistenceMapper.toDomain(saved);
	}

	@Override
	public DeliveryAssignment update(DeliveryAssignment assignment) {
		Objects.requireNonNull(assignment, "assignment is required");
		DeliveryAssignmentJpaEntity existing = springDataDeliveryAssignmentRepository
				.findById(assignment.id())
				.orElseThrow(() -> new ResourceNotFoundException("DeliveryAssignment", assignment.id()));
		deliveryAssignmentPersistenceMapper.copyToEntity(assignment, existing);
		DeliveryAssignmentJpaEntity saved = springDataDeliveryAssignmentRepository.save(existing);
		return deliveryAssignmentPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<DeliveryAssignment> findById(UUID id) {
		Objects.requireNonNull(id, "id is required");
		return springDataDeliveryAssignmentRepository.findById(id)
				.map(deliveryAssignmentPersistenceMapper::toDomain);
	}

	@Override
	public List<DeliveryAssignment> findAll() {
		return springDataDeliveryAssignmentRepository
				.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
				.stream()
				.map(deliveryAssignmentPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public boolean existsActiveByOrderId(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		return springDataDeliveryAssignmentRepository.existsByOrderIdAndStatusIn(
				orderId,
				ACTIVE_STATUSES);
	}

	@Override
	public Optional<DeliveryAssignment> findActiveByOrderId(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		return springDataDeliveryAssignmentRepository
				.findFirstByOrderIdAndStatusIn(orderId, ACTIVE_STATUSES)
				.map(deliveryAssignmentPersistenceMapper::toDomain);
	}

	@Override
	public Optional<DeliveryAssignment> findActiveByDriverId(UUID driverId) {
		Objects.requireNonNull(driverId, "driverId is required");
		return springDataDeliveryAssignmentRepository
				.findFirstByDriverIdAndStatusIn(driverId, ACTIVE_STATUSES)
				.map(deliveryAssignmentPersistenceMapper::toDomain);
	}

	@Override
	public List<DeliveryAssignment> findAllByDriverId(UUID driverId) {
		Objects.requireNonNull(driverId, "driverId is required");
		return springDataDeliveryAssignmentRepository.findAllByDriverIdOrderByCreatedAtDesc(driverId)
				.stream().map(deliveryAssignmentPersistenceMapper::toDomain).toList();
	}
}
