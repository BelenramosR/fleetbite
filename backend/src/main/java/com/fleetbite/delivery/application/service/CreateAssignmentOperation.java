package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.DriverNotAssignableException;
import com.fleetbite.delivery.domain.exception.OrderNotAssignableException;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Shared application operation used by manual and auto assignment once Order + Driver are known.
 *
 * <p>Manual assign passes {@code assignmentScore = null}. Auto assign passes {@code distanceKm}
 * (stored temporarily as assignmentScore).
 */
public final class CreateAssignmentOperation {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final OrderHistoryRecorder orderHistoryRecorder;
	private final Clock clock;

	public CreateAssignmentOperation(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
		this.orderHistoryRecorder = Objects.requireNonNull(orderHistoryRecorder);
		this.clock = Objects.requireNonNull(clock);
	}

	public DeliveryAssignment execute(Order order, Driver driver, BigDecimal assignmentScore) {
		Objects.requireNonNull(order, "order is required");
		Objects.requireNonNull(driver, "driver is required");

		if (order.status() != OrderStatus.READY && order.status() != OrderStatus.WAITING_FOR_DRIVER) {
			throw new OrderNotAssignableException(order.status());
		}
		if (driver.status() != DriverStatus.AVAILABLE) {
			throw new DriverNotAssignableException(driver.status());
		}
		if (driver.currentLocation() == null) {
			throw new DriverNotAssignableException("Driver cannot be assigned without a current location");
		}
		if (assignmentRepositoryPort.existsActiveByOrderId(order.id())) {
			throw new ActiveAssignmentAlreadyExistsException(order.id().value());
		}

		OrderStatus previous = order.status();
		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		DeliveryAssignment assignment = DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				order.id(),
				driver.id(),
				now,
				assignmentScore);

		order.assign(now);
		driver.markBusy(now);

		DeliveryAssignment saved = assignmentRepositoryPort.save(assignment);
		orderRepositoryPort.update(order);
		driverRepositoryPort.update(driver);
		orderHistoryRecorder.record(
				order.id(),
				OrderHistoryEventType.DRIVER_ASSIGNED,
				previous,
				order.status(),
				null,
				now);
		return saved;
	}
}
