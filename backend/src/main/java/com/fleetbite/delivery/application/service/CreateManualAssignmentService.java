package com.fleetbite.delivery.application.service;

import java.util.UUID;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;

import java.util.Objects;

public final class CreateManualAssignmentService implements CreateManualAssignmentUseCase {

	private final OrderRepositoryPort orderRepositoryPort;
	private final DriverRepositoryPort driverRepositoryPort;
	private final CreateAssignmentOperation createAssignmentOperation;

	public CreateManualAssignmentService(
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			CreateAssignmentOperation createAssignmentOperation) {
		this.orderRepositoryPort = Objects.requireNonNull(orderRepositoryPort);
		this.driverRepositoryPort = Objects.requireNonNull(driverRepositoryPort);
		this.createAssignmentOperation = Objects.requireNonNull(createAssignmentOperation);
	}

	@Override
	public AssignmentResult execute(CreateManualAssignmentCommand command) {
		Objects.requireNonNull(command, "command is required");
		Objects.requireNonNull(command.orderId(), "orderId is required");
		Objects.requireNonNull(command.driverId(), "driverId is required");

		UUID orderId = command.orderId();
		UUID driverId = command.driverId();

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));

		DeliveryAssignment saved = createAssignmentOperation.execute(order, driver, null);
		return AssignmentResult.from(saved);
	}
}
