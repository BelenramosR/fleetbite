package com.fleetbite.order.infrastructure.config;

import com.fleetbite.order.application.port.in.CancelOrderUseCase;
import com.fleetbite.order.application.port.in.ConfirmOrderUseCase;
import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.DeleteOrderUseCase;
import com.fleetbite.order.application.port.in.GetOrderByIdUseCase;
import com.fleetbite.order.application.port.in.GetOrderHistoryUseCase;
import com.fleetbite.order.application.port.in.ListOrdersUseCase;
import com.fleetbite.order.application.port.in.MarkOrderReadyUseCase;
import com.fleetbite.order.application.port.in.StartOrderPreparationUseCase;
import com.fleetbite.order.application.port.in.UpdateOrderUseCase;
import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.CancelOrderService;
import com.fleetbite.order.application.service.ConfirmOrderService;
import com.fleetbite.order.application.service.CreateOrderService;
import com.fleetbite.order.application.service.DeleteOrderService;
import com.fleetbite.order.application.service.GetOrderByIdService;
import com.fleetbite.order.application.service.GetOrderHistoryService;
import com.fleetbite.order.application.service.ListOrdersService;
import com.fleetbite.order.application.service.MarkOrderReadyService;
import com.fleetbite.order.application.service.OrderHistoryRecorder;
import com.fleetbite.order.application.service.StartOrderPreparationService;
import com.fleetbite.order.application.service.UpdateOrderService;
import com.fleetbite.order.infrastructure.transaction.TransactionalCancelOrderUseCase;
import com.fleetbite.order.infrastructure.transaction.TransactionalConfirmOrderUseCase;
import com.fleetbite.order.infrastructure.transaction.TransactionalCreateOrderUseCase;
import com.fleetbite.order.infrastructure.transaction.TransactionalMarkOrderReadyUseCase;
import com.fleetbite.order.infrastructure.transaction.TransactionalStartOrderPreparationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
			Clock clock) {
		return new TransactionalCreateOrderUseCase(
				new CreateOrderService(orderRepositoryPort, orderHistoryRecorder, clock));
	}

	@Bean
	GetOrderByIdUseCase getOrderByIdUseCase(OrderRepositoryPort orderRepositoryPort) {
		return new GetOrderByIdService(orderRepositoryPort);
	}

	@Bean
	ListOrdersUseCase listOrdersUseCase(OrderRepositoryPort orderRepositoryPort) {
		return new ListOrdersService(orderRepositoryPort);
	}

	@Bean
	UpdateOrderUseCase updateOrderUseCase(OrderRepositoryPort orderRepositoryPort) {
		return new UpdateOrderService(orderRepositoryPort);
	}

	@Bean
	DeleteOrderUseCase deleteOrderUseCase(OrderRepositoryPort orderRepositoryPort) {
		return new DeleteOrderService(orderRepositoryPort);
	}

	@Bean
	ConfirmOrderUseCase confirmOrderUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		return new TransactionalConfirmOrderUseCase(
				new ConfirmOrderService(orderRepositoryPort, orderHistoryRecorder, clock));
	}

	@Bean
	StartOrderPreparationUseCase startOrderPreparationUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		return new TransactionalStartOrderPreparationUseCase(
				new StartOrderPreparationService(orderRepositoryPort, orderHistoryRecorder, clock));
	}

	@Bean
	MarkOrderReadyUseCase markOrderReadyUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		return new TransactionalMarkOrderReadyUseCase(
				new MarkOrderReadyService(orderRepositoryPort, orderHistoryRecorder, clock));
	}

	@Bean
	CancelOrderUseCase cancelOrderUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRecorder orderHistoryRecorder,
			Clock clock) {
		return new TransactionalCancelOrderUseCase(
				new CancelOrderService(orderRepositoryPort, orderHistoryRecorder, clock));
	}

	@Bean
	GetOrderHistoryUseCase getOrderHistoryUseCase(
			OrderRepositoryPort orderRepositoryPort,
			OrderHistoryRepositoryPort orderHistoryRepositoryPort) {
		return new GetOrderHistoryService(orderRepositoryPort, orderHistoryRepositoryPort);
	}
}
