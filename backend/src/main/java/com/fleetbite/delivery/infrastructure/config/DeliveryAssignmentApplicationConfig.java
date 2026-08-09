package com.fleetbite.delivery.infrastructure.config;

import com.fleetbite.delivery.application.port.in.AcceptAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.CompleteAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.GetAssignmentByIdUseCase;
import com.fleetbite.delivery.application.port.in.ListAssignmentsUseCase;
import com.fleetbite.delivery.application.port.in.PickupAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.RejectAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.StartDeliveryAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.application.service.AcceptAssignmentService;
import com.fleetbite.delivery.application.service.CompleteAssignmentService;
import com.fleetbite.delivery.application.service.CreateManualAssignmentService;
import com.fleetbite.delivery.application.service.GetAssignmentByIdService;
import com.fleetbite.delivery.application.service.ListAssignmentsService;
import com.fleetbite.delivery.application.service.PickupAssignmentService;
import com.fleetbite.delivery.application.service.RejectAssignmentService;
import com.fleetbite.delivery.application.service.StartDeliveryAssignmentService;
import com.fleetbite.delivery.infrastructure.transaction.TransactionalAcceptAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.transaction.TransactionalCompleteAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.transaction.TransactionalCreateManualAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.transaction.TransactionalPickupAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.transaction.TransactionalRejectAssignmentUseCase;
import com.fleetbite.delivery.infrastructure.transaction.TransactionalStartDeliveryAssignmentUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DeliveryAssignmentApplicationConfig {

	@Bean
	CreateManualAssignmentUseCase createManualAssignmentUseCase(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		return new TransactionalCreateManualAssignmentUseCase(
				new CreateManualAssignmentService(
						assignmentRepositoryPort,
						orderRepositoryPort,
						driverRepositoryPort,
						clock));
	}

	@Bean
	GetAssignmentByIdUseCase getAssignmentByIdUseCase(DeliveryAssignmentRepositoryPort assignmentRepositoryPort) {
		return new GetAssignmentByIdService(assignmentRepositoryPort);
	}

	@Bean
	ListAssignmentsUseCase listAssignmentsUseCase(DeliveryAssignmentRepositoryPort assignmentRepositoryPort) {
		return new ListAssignmentsService(assignmentRepositoryPort);
	}

	@Bean
	AcceptAssignmentUseCase acceptAssignmentUseCase(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			Clock clock) {
		return new TransactionalAcceptAssignmentUseCase(
				new AcceptAssignmentService(assignmentRepositoryPort, clock));
	}

	@Bean
	RejectAssignmentUseCase rejectAssignmentUseCase(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		return new TransactionalRejectAssignmentUseCase(
				new RejectAssignmentService(
						assignmentRepositoryPort,
						orderRepositoryPort,
						driverRepositoryPort,
						clock));
	}

	@Bean
	PickupAssignmentUseCase pickupAssignmentUseCase(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			Clock clock) {
		return new TransactionalPickupAssignmentUseCase(
				new PickupAssignmentService(assignmentRepositoryPort, orderRepositoryPort, clock));
	}

	@Bean
	StartDeliveryAssignmentUseCase startDeliveryAssignmentUseCase(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			Clock clock) {
		return new TransactionalStartDeliveryAssignmentUseCase(
				new StartDeliveryAssignmentService(assignmentRepositoryPort, orderRepositoryPort, clock));
	}

	@Bean
	CompleteAssignmentUseCase completeAssignmentUseCase(
			DeliveryAssignmentRepositoryPort assignmentRepositoryPort,
			OrderRepositoryPort orderRepositoryPort,
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		return new TransactionalCompleteAssignmentUseCase(
				new CompleteAssignmentService(
						assignmentRepositoryPort,
						orderRepositoryPort,
						driverRepositoryPort,
						clock));
	}
}
