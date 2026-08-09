package com.fleetbite.order.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOrderHistoryRepository extends JpaRepository<OrderHistoryJpaEntity, UUID> {

	List<OrderHistoryJpaEntity> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
