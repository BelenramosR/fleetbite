package com.fleetbite.order.infrastructure.config;

import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.DeleteOrderUseCase;
import com.fleetbite.order.application.port.in.OrderQueryUseCase;
import com.fleetbite.order.application.port.in.OrderWorkflowUseCase;
import com.fleetbite.order.application.port.in.UpdateOrderUseCase;
import com.fleetbite.order.application.port.out.DomainEventPublisherPort;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.CreateOrderService;
import com.fleetbite.order.application.service.DeleteOrderService;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.application.service.OrderQueryService;
import com.fleetbite.order.application.service.OrderWorkflowService;
import com.fleetbite.order.application.service.UpdateOrderService;
import com.fleetbite.order.infrastructure.transaction.OrderTransactionProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

@Configuration
public class OrderApplicationConfig {

	@Bean
	OrderHistoryRecorder orderHistoryRecorder(OrderHistoryRepositoryPort orderHistoryRepositoryPort) {
		return new OrderHistoryRecorder(orderHistoryRepositoryPort);
	}

	@Bean
	CreateOrderUseCase createOrderUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock,
			PlatformTransactionManager transactionManager) {
		return OrderTransactionProxyFactory.readWrite(
				CreateOrderUseCase.class,
				new CreateOrderService(orderRepositoryPort, orderHistoryRecorder, clock),
				transactionManager);
	}

	@Bean
	OrderQueryUseCase orderQueryUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRepositoryPort orderHistoryRepositoryPort,
			PlatformTransactionManager transactionManager) {
		return OrderTransactionProxyFactory.readOnly(
				OrderQueryUseCase.class,
				new OrderQueryService(orderRepositoryPort, orderHistoryRepositoryPort),
				transactionManager);
	}

	@Bean
	UpdateOrderUseCase updateOrderUseCase(
			OrderRepositoryPort orderRepositoryPort,
			PlatformTransactionManager transactionManager) {
		return OrderTransactionProxyFactory.readWrite(
				UpdateOrderUseCase.class,
				new UpdateOrderService(orderRepositoryPort),
				transactionManager);
	}

	@Bean
	DeleteOrderUseCase deleteOrderUseCase(
			OrderRepositoryPort orderRepositoryPort,
			PlatformTransactionManager transactionManager) {
		return OrderTransactionProxyFactory.readWrite(
				DeleteOrderUseCase.class,
				new DeleteOrderService(orderRepositoryPort),
				transactionManager);
	}

	@Bean
	OrderWorkflowUseCase orderWorkflowUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			DomainEventPublisherPort domainEventPublisherPort,
			Clock clock,
			PlatformTransactionManager transactionManager) {
		return OrderTransactionProxyFactory.readWrite(
				OrderWorkflowUseCase.class,
				new OrderWorkflowService(
						orderRepositoryPort,
						orderHistoryRecorder,
						domainEventPublisherPort,
						clock),
				transactionManager);
	}
}
