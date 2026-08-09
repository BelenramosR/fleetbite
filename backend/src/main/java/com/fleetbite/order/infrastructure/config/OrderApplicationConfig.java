package com.fleetbite.order.infrastructure.config;

import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.GetOrderByIdUseCase;
import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.application.service.CreateOrderService;
import com.fleetbite.order.application.service.GetOrderByIdService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class OrderApplicationConfig {

	@Bean
	CreateOrderUseCase createOrderUseCase(OrderRepositoryPort orderRepositoryPort, Clock clock) {
		return new CreateOrderService(orderRepositoryPort, clock);
	}

	@Bean
	GetOrderByIdUseCase getOrderByIdUseCase(OrderRepositoryPort orderRepositoryPort) {
		return new GetOrderByIdService(orderRepositoryPort);
	}
}
