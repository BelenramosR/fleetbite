package com.fleetbite.order.infrastructure.inbound.rest;

import com.fleetbite.order.application.dto.OrderResult;
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
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.order.infrastructure.inbound.rest.request.CancelOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.request.CreateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.request.UpdateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderHistoryResponse;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final CreateOrderUseCase createOrderUseCase;
	private final GetOrderByIdUseCase getOrderByIdUseCase;
	private final ListOrdersUseCase listOrdersUseCase;
	private final UpdateOrderUseCase updateOrderUseCase;
	private final DeleteOrderUseCase deleteOrderUseCase;
	private final ConfirmOrderUseCase confirmOrderUseCase;
	private final StartOrderPreparationUseCase startOrderPreparationUseCase;
	private final MarkOrderReadyUseCase markOrderReadyUseCase;
	private final CancelOrderUseCase cancelOrderUseCase;
	private final GetOrderHistoryUseCase getOrderHistoryUseCase;
	private final OrderHttpMapper orderHttpMapper;

	public OrderController(
			CreateOrderUseCase createOrderUseCase,
			GetOrderByIdUseCase getOrderByIdUseCase,
			ListOrdersUseCase listOrdersUseCase,
			UpdateOrderUseCase updateOrderUseCase,
			DeleteOrderUseCase deleteOrderUseCase,
			ConfirmOrderUseCase confirmOrderUseCase,
			StartOrderPreparationUseCase startOrderPreparationUseCase,
			MarkOrderReadyUseCase markOrderReadyUseCase,
			CancelOrderUseCase cancelOrderUseCase,
			GetOrderHistoryUseCase getOrderHistoryUseCase,
			OrderHttpMapper orderHttpMapper) {
		this.createOrderUseCase = Objects.requireNonNull(createOrderUseCase);
		this.getOrderByIdUseCase = Objects.requireNonNull(getOrderByIdUseCase);
		this.listOrdersUseCase = Objects.requireNonNull(listOrdersUseCase);
		this.updateOrderUseCase = Objects.requireNonNull(updateOrderUseCase);
		this.deleteOrderUseCase = Objects.requireNonNull(deleteOrderUseCase);
		this.confirmOrderUseCase = Objects.requireNonNull(confirmOrderUseCase);
		this.startOrderPreparationUseCase = Objects.requireNonNull(startOrderPreparationUseCase);
		this.markOrderReadyUseCase = Objects.requireNonNull(markOrderReadyUseCase);
		this.cancelOrderUseCase = Objects.requireNonNull(cancelOrderUseCase);
		this.getOrderHistoryUseCase = Objects.requireNonNull(getOrderHistoryUseCase);
		this.orderHttpMapper = Objects.requireNonNull(orderHttpMapper);
	}

	@GetMapping
	public List<OrderResponse> listOrders() {
		return listOrdersUseCase.execute().stream()
				.map(orderHttpMapper::toResponse)
				.toList();
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

	@PutMapping("/{id}")
	public OrderResponse updateOrder(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateOrderRequest request) {
		OrderResult result = updateOrderUseCase.execute(OrderId.of(id), orderHttpMapper.toCommand(request));
		return orderHttpMapper.toResponse(result);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteOrder(@PathVariable UUID id) {
		deleteOrderUseCase.execute(OrderId.of(id));
	}

	@PostMapping("/{id}/confirm")
	public OrderResponse confirm(@PathVariable UUID id) {
		return orderHttpMapper.toResponse(confirmOrderUseCase.execute(OrderId.of(id)));
	}

	@PostMapping("/{id}/start-preparation")
	public OrderResponse startPreparation(@PathVariable UUID id) {
		return orderHttpMapper.toResponse(startOrderPreparationUseCase.execute(OrderId.of(id)));
	}

	@PostMapping("/{id}/ready")
	public OrderResponse markReady(@PathVariable UUID id) {
		return orderHttpMapper.toResponse(markOrderReadyUseCase.execute(OrderId.of(id)));
	}

	@PostMapping("/{id}/cancel")
	public OrderResponse cancel(
			@PathVariable UUID id,
			@Valid @RequestBody(required = false) CancelOrderRequest request) {
		return orderHttpMapper.toResponse(
				cancelOrderUseCase.execute(OrderId.of(id), orderHttpMapper.toCommand(request)));
	}

	@GetMapping("/{id}/history")
	public List<OrderHistoryResponse> history(@PathVariable UUID id) {
		return getOrderHistoryUseCase.execute(OrderId.of(id)).stream()
				.map(orderHttpMapper::toResponse)
				.toList();
	}
}
