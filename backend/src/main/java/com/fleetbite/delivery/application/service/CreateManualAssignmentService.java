package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.domain.exception.ActiveAssignmentAlreadyExistsException;
import com.fleetbite.delivery.domain.exception.DriverNotAssignableException;
import com.fleetbite.delivery.domain.exception.OrderNotAssignableException;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.delivery.domain.model.DeliveryAssignmentId;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.driver.domain.model.DriverStatus;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.domain.model.OrderStatus;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

public final class CreateManualAssignmentService implements CreateManualAssignmentUseCase {

	private final DeliveryAssignmentRepositoryPort assignmentRepositoryPort;
	private final OrderRepositoryPort orderRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final Clock clock;

	public CreateManualAssignmentService(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		this.assignmentRepositoryPort = Objects.requireNonNull(assignmentRepositoryPort);
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public AssignmentResult execute(CreateManualAssignmentCommand command) {
		Objects.requireNonNull(command, "command is required");
		Objects.requireNonNull(command.orderId(), "orderId is required");
		Objects.requireNonNull(command.driverId(), "driverId is required");

		OrderId orderId = OrderId.of(command.orderId());
		DriverId driverId = DriverId.of(command.driverId());

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId.value()));
		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));

		if (order.status() != OrderStatus.READY && order.status() != OrderStatus.WAITING_FOR_DRIVER) {
			throw new OrderNotAssignableException(order.status());
		}
		if (driver.status() != DriverStatus.AVAILABLE) {
			throw new DriverNotAssignableException(driver.status());
		}
		if (driver.currentLocation() == null) {
			throw new DriverNotAssignableException("Driver cannot be assigned without a current location");
		}
		if (assignmentRepositoryPort.existsActiveByOrderId(orderId)) {
			throw new ActiveAssignmentAlreadyExistsException(orderId.value());
		}

		OffsetDateTime now = BusinessTime.toBusinessTime(clock.instant());
		DeliveryAssignment assignment = DeliveryAssignment.create(
				DeliveryAssignmentId.generate(),
				orderId,
				driverId,
				now);

		order.assign(now);
		driver.markBusy(now);

		DeliveryAssignment saved = assignmentRepositoryPort.save(assignment);
		orderRepositoryPort.update(order);
		driverRepositoryPort.update(driver);

		return AssignmentResult.from(saved);
	}
}
