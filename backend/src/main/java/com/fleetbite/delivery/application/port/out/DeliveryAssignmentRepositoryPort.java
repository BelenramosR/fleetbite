package com.fleetbite.delivery.application.port.out;

import java.util.UUID;

import com.fleetbite.delivery.domain.model.DeliveryAssignment;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepositoryPort {

	DeliveryAssignment save(DeliveryAssignment assignment);

	DeliveryAssignment update(DeliveryAssignment assignment);

	Optional<DeliveryAssignment> findById(UUID id);

	List<DeliveryAssignment> findAll();

	boolean existsActiveByOrderId(UUID orderId);

	Optional<DeliveryAssignment> findActiveByOrderId(UUID orderId);

	Optional<DeliveryAssignment> findActiveByDriverId(UUID driverId);

	List<DeliveryAssignment> findAllByDriverId(UUID driverId);
}
