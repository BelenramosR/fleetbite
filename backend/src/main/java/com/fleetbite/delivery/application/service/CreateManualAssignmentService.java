package com.fleetbite.delivery.application.service;

import com.fleetbite.delivery.application.dto.AssignmentResult;
import com.fleetbite.delivery.application.dto.CreateManualAssignmentCommand;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.domain.model.DeliveryAssignment;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.domain.model.Driver;
import com.fleetbite.driver.domain.model.DriverId;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;
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

		OrderId orderId = OrderId.of(command.orderId());
		DriverId driverId = DriverId.of(command.driverId());

		Order order = orderRepositoryPort.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", orderId.value()));
		Driver driver = driverRepositoryPort.findById(driverId)
				.orElseThrow(() -> new ResourceNotFoundException("Driver", driverId.value()));

		DeliveryAssignment saved = createAssignmentOperation.execute(order, driver, null);
		return AssignmentResult.from(saved);
	}
}
