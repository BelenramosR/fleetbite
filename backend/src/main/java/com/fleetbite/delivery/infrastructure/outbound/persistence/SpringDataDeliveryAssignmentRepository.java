package com.fleetbite.delivery.infrastructure.outbound.persistence;

import com.fleetbite.delivery.domain.model.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataDeliveryAssignmentRepository extends JpaRepository<DeliveryAssignmentJpaEntity, UUID> {

	boolean existsByOrderIdAndStatusIn(UUID orderId, Collection<AssignmentStatus> statuses);

	Optional<DeliveryAssignmentJpaEntity> findFirstByOrderIdAndStatusIn(
			UUID orderId,
			Collection<AssignmentStatus> statuses);

	Optional<DeliveryAssignmentJpaEntity> findFirstByDriverIdAndStatusIn(
			UUID driverId,
			Collection<AssignmentStatus> statuses);
}
