package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AutoAssignmentResult;
import com.fleetbite.delivery.application.policy.DriverCandidate;
import com.fleetbite.delivery.application.policy.DriverSelectionPolicy;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.OrderNotAssignableException;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AutoAssignOrderService implements AutoAssignOrderUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final DriverSelectionPolicy driverSelectionPolicy;
	private final CreateAssignmentOperation createAssignmentOperation;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public AutoAssignOrderService(
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			DriverSelectionPolicy driverSelectionPolicy,
			CreateAssignmentOperation createAssignmentOperation,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.driverSelectionPolicy = Objects.requireNonNull(driverSelectionPolicy);
		this.createAssignmentOperation = Objects.requireNonNull(createAssignmentOperation);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public AutoAssignmentResult execute(UUID orderId) {
		Objects.requireNonNull(orderId, "orderId is required");

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

		if (order.status() != OrderStatus.READY && order.status() != OrderStatus.WAITING_FOR_DRIVER) {
			throw new OrderNotAssignableException(order.status());
		}
		if (assignmentRepositoryPort.existsActiveByOrderId(orderId)) {
			throw new ActiveAssignmentAlreadyExistsException(orderId);
		}

		List<Driver> candidates = driverRepositoryPort.findAvailableWithLocation();
		Optional<DriverCandidate> selected = driverSelectionPolicy.select(order, candidates);

		if (selected.isEmpty()) {
			if (order.status() == OrderStatus.READY) {
				OrderStatus previous = order.status();
				order.markWaitingForDriver();
				orderRepositoryPort.update(order);
				OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
				orderHistoryRecorder.record(
						orderId,
						OrderHistoryEventType.ORDER_WAITING_FOR_DRIVER,
						previous,
						order.status(),
						null,
						now);
			}
			return AutoAssignmentResult.waitingForDriver(orderId);
		}

		DriverCandidate candidate = selected.get();
		DeliveryAssignment assignment = createAssignmentOperation.execute(
				order,
				candidate.driver(),
				candidate.score());

		return AutoAssignmentResult.assigned(
				orderId,
				assignment.id(),
				candidate.driver().id(),
				candidate.distanceKm(),
				candidate.score());
	}
}
