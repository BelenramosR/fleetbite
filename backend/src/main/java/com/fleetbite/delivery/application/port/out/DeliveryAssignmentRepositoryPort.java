package com.fleetbite.delivery.application.port.out;

import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.order.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepositoryPort {

	DeliveryAssignment save(DeliveryAssignment assignment);

	DeliveryAssignment update(DeliveryAssignment assignment);

	Optional<DeliveryAssignment> findById(DeliveryAssignmentId id);

	List<DeliveryAssignment> findAll();

	boolean existsActiveByOrderId(OrderId orderId);

	Optional<DeliveryAssignment> findActiveByOrderId(OrderId orderId);
}
