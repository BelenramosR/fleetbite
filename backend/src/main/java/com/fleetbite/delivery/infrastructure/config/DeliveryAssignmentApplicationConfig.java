package com.fleetbite.delivery.infrastructure.config;

import com.fleetbite.delivery.application.policy.DriverSelectionPolicy;
import com.fleetbite.delivery.application.policy.NearestDriverSelectionPolicy;
import com.fleetbite.delivery.application.port.in.AssignmentQueryUseCase;
import com.fleetbite.delivery.application.port.in.AssignmentWorkflowUseCase;
import com.fleetbite.delivery.application.port.in.AutoAssignOrderUseCase;
import com.fleetbite.delivery.application.port.in.CreateManualAssignmentUseCase;
import com.fleetbite.delivery.application.port.in.DriverAssignmentUseCase;
import com.fleetbite.delivery.application.port.out.DeliveryAssignmentRepositoryPort;
import com.fleetbite.delivery.application.port.out.DistanceCalculatorPort;
import com.fleetbite.delivery.application.service.AcceptAssignmentService;
import com.fleetbite.delivery.application.service.AssignmentQueryService;
import com.fleetbite.delivery.application.service.AssignmentWorkflowService;
import com.fleetbite.delivery.application.service.AutoAssignOrderService;
import com.fleetbite.delivery.application.service.CompleteAssignmentService;
import com.fleetbite.delivery.application.service.CreateAssignmentOperation;
import com.fleetbite.delivery.application.service.CreateManualAssignmentService;
import com.fleetbite.delivery.application.service.DriverAssignmentService;
import com.fleetbite.delivery.application.service.PickupAssignmentService;
import com.fleetbite.delivery.application.service.RejectAssignmentService;
import com.fleetbite.delivery.application.service.StartDeliveryAssignmentService;
import com.fleetbite.delivery.infrastructure.transaction.DeliveryTransactionProxyFactory;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.shared.domain.model.Location;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

@Configuration
public class DeliveryAssignmentApplicationConfig {

	@Bean
	CreateAssignmentOperation createAssignmentOperation(
			DeliveryAssignmentRepositoryPort assignments,
			OrderRepositoryPort orders,
			DriverRepositoryPort drivers,
			OrderHistoryRecorder history,
			Clock clock) {
		return new CreateAssignmentOperation(assignments, orders, drivers, history, clock);
	}

	@Bean
	DriverSelectionPolicy driverSelectionPolicy(
			DistanceCalculatorPort distances,
			@Qualifier("restaurantLocation") Location restaurantLocation) {
		return new NearestDriverSelectionPolicy(distances, restaurantLocation);
	}

	@Bean
	CreateManualAssignmentUseCase createManualAssignmentUseCase(
			OrderRepositoryPort orders,
			DriverRepositoryPort drivers,
			CreateAssignmentOperation operation,
			PlatformTransactionManager transactions) {
		return DeliveryTransactionProxyFactory.readWrite(
				CreateManualAssignmentUseCase.class,
				new CreateManualAssignmentService(orders, drivers, operation),
				transactions);
	}

	@Bean
	AutoAssignOrderUseCase autoAssignOrderUseCase(
			OrderRepositoryPort orders,
			DriverRepositoryPort drivers,
			DeliveryAssignmentRepositoryPort assignments,
			DriverSelectionPolicy selectionPolicy,
			CreateAssignmentOperation operation,
			OrderHistoryRecorder history,
			Clock clock,
			PlatformTransactionManager transactions) {
		return DeliveryTransactionProxyFactory.requiresNew(
				AutoAssignOrderUseCase.class,
				new AutoAssignOrderService(
						orders, drivers, assignments, selectionPolicy, operation, history, clock),
				transactions);
	}

	@Bean
	AssignmentQueryUseCase assignmentQueryUseCase(
			DeliveryAssignmentRepositoryPort assignments,
			PlatformTransactionManager transactions) {
		return DeliveryTransactionProxyFactory.readOnly(
				AssignmentQueryUseCase.class,
				new AssignmentQueryService(assignments),
				transactions);
	}

	@Bean
	AssignmentWorkflowUseCase assignmentWorkflowUseCase(
			DeliveryAssignmentRepositoryPort assignments,
			OrderRepositoryPort orders,
			DriverRepositoryPort drivers,
			OrderHistoryRecorder history,
			Clock clock,
			PlatformTransactionManager transactions) {
		AssignmentWorkflowService workflow = new AssignmentWorkflowService(
				new AcceptAssignmentService(assignments, history, clock),
				new RejectAssignmentService(assignments, orders, drivers, history, clock),
				new PickupAssignmentService(assignments, orders, history, clock),
				new StartDeliveryAssignmentService(assignments, orders, history, clock),
				new CompleteAssignmentService(assignments, orders, drivers, history, clock));
		return DeliveryTransactionProxyFactory.readWrite(
				AssignmentWorkflowUseCase.class, workflow, transactions);
	}

	@Bean
	DriverAssignmentUseCase driverAssignmentUseCase(
			DriverRepositoryPort drivers,
			DeliveryAssignmentRepositoryPort assignments,
			OrderRepositoryPort orders,
			AssignmentWorkflowUseCase workflow,
			Clock clock,
			PlatformTransactionManager transactions) {
		return DeliveryTransactionProxyFactory.readWrite(
				DriverAssignmentUseCase.class,
				new DriverAssignmentService(drivers, assignments, orders, workflow, clock), transactions);
	}
}
