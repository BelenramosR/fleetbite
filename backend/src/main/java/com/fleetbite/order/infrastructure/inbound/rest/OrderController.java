package com.fleetbite.order.infrastructure.inbound.rest;

import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.GetOrderByIdUseCase;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.infrastructure.inbound.rest.request.CreateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final CreateOrderUseCase createOrderUseCase;
	private final GetOrderByIdUseCase getOrderByIdUseCase;
	private final OrderHttpMapper orderHttpMapper;

	public OrderController(
			CreateOrderUseCase createOrderUseCase,
			GetOrderByIdUseCase getOrderByIdUseCase,
			OrderHttpMapper orderHttpMapper) {
		this.createOrderUseCase = Objects.requireNonNull(createOrderUseCase);
		this.getOrderByIdUseCase = Objects.requireNonNull(getOrderByIdUseCase);
		this.orderHttpMapper = Objects.requireNonNull(orderHttpMapper);
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		OrderResult result = createOrderUseCase.execute(orderHttpMapper.toCommand(request));
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(result.id())
				.toUri();
		return ResponseEntity.created(location).body(orderHttpMapper.toResponse(result));
	}

	@GetMapping("/{id}")
	public OrderResponse getOrderById(@PathVariable UUID id) {
		OrderResult result = getOrderByIdUseCase.execute(OrderId.of(id));
		return orderHttpMapper.toResponse(result);
	}
}
