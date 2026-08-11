package com.fleetbite.order.infrastructure.inbound.rest;

import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.port.in.CreateOrderUseCase;
import com.fleetbite.order.application.port.in.DeleteOrderUseCase;
import com.fleetbite.order.application.port.in.OrderQueryUseCase;
import com.fleetbite.order.application.port.in.OrderWorkflowUseCase;
import com.fleetbite.order.application.port.in.UpdateOrderUseCase;
import com.fleetbite.order.infrastructure.inbound.rest.request.CancelOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.request.CreateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.request.UpdateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderHistoryResponse;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderResponse;
import com.fleetbite.shared.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class OrderController {

	private final CreateOrderUseCase createOrderUseCase;
	private final OrderQueryUseCase orderQueryUseCase;
	private final UpdateOrderUseCase updateOrderUseCase;
	private final DeleteOrderUseCase deleteOrderUseCase;
	private final OrderWorkflowUseCase orderWorkflowUseCase;
	private final OrderHttpMapper orderHttpMapper;

	public OrderController(
			CreateOrderUseCase createOrderUseCase,
			OrderQueryUseCase orderQueryUseCase,
			UpdateOrderUseCase updateOrderUseCase,
			DeleteOrderUseCase deleteOrderUseCase,
			OrderWorkflowUseCase orderWorkflowUseCase,
			OrderHttpMapper orderHttpMapper) {
		this.createOrderUseCase = Objects.requireNonNull(createOrderUseCase);
		this.orderQueryUseCase = Objects.requireNonNull(orderQueryUseCase);
		this.updateOrderUseCase = Objects.requireNonNull(updateOrderUseCase);
		this.deleteOrderUseCase = Objects.requireNonNull(deleteOrderUseCase);
		this.orderWorkflowUseCase = Objects.requireNonNull(orderWorkflowUseCase);
		this.orderHttpMapper = Objects.requireNonNull(orderHttpMapper);
	}

	@GetMapping
	@Operation(summary = "List orders")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Orders returned"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public List<OrderResponse> listOrders(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "100") int size) {
		return orderQueryUseCase.findPage(page, size).stream()
				.map(orderHttpMapper::toResponse)
				.toList();
	}

	@PostMapping
	@Operation(summary = "Create order",
			description = "Creates order in CREATED status. promisedDeliveryAt is calculated by the backend.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Order created",
					content = @Content(schema = @Schema(implementation = OrderResponse.class))),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
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
	@Operation(summary = "Get order by id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Order found"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public OrderResponse getOrderById(@PathVariable UUID id) {
		OrderResult result = orderQueryUseCase.getById(id);
		return orderHttpMapper.toResponse(result);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update order", description = "Allowed only while order is in CREATED status.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Order updated"),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Conflict",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public OrderResponse updateOrder(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateOrderRequest request) {
		OrderResult result = updateOrderUseCase.execute(id, orderHttpMapper.toCommand(request));
		return orderHttpMapper.toResponse(result);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete order", description = "Allowed only while order is in CREATED status.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Order deleted"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Conflict",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public void deleteOrder(@PathVariable UUID id) {
		deleteOrderUseCase.execute(id);
	}

	@PostMapping("/{id}/confirm")
	@Operation(summary = "Confirm order", description = "CREATED → CONFIRMED")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Order confirmed"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public OrderResponse confirm(@PathVariable UUID id) {
		return orderHttpMapper.toResponse(orderWorkflowUseCase.confirm(id));
	}

	@PostMapping("/{id}/start-preparation")
	@Operation(summary = "Start preparation", description = "CONFIRMED → PREPARING")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Preparation started"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public OrderResponse startPreparation(@PathVariable UUID id) {
		return orderHttpMapper.toResponse(orderWorkflowUseCase.startPreparation(id));
	}

	@PostMapping("/{id}/ready")
	@Operation(summary = "Mark order ready",
			description = """
					PREPARING → READY within the request transaction (TX A): persists the order,
					records history ORDER_READY, and publishes domain event ORDER_READY.
					After commit, a local listener runs AutoAssignOrderUseCase (TX B), which may
					leave the order ASSIGNED or WAITING_FOR_DRIVER.
					The HTTP response reflects TX A and may still show status READY even though
					the database may already contain ASSIGNED or WAITING_FOR_DRIVER (eventual consistency).
					Manual fallback remains available at POST /api/v1/orders/{id}/auto-assign.
					""")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
					description = "Order marked READY (TX A). Auto-assignment runs after commit."),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public OrderResponse markReady(@PathVariable UUID id) {
		return orderHttpMapper.toResponse(orderWorkflowUseCase.markReady(id));
	}

	@PostMapping("/{id}/cancel")
	@Operation(summary = "Cancel order",
			description = "Cancels when allowed by domain rules. Optional reason is stored in order history only.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Order cancelled"),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Invalid transition",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public OrderResponse cancel(
			@PathVariable UUID id,
			@Valid @RequestBody(required = false) CancelOrderRequest request) {
		return orderHttpMapper.toResponse(
				orderWorkflowUseCase.cancel(id, orderHttpMapper.toCommand(request)));
	}

	@GetMapping("/{id}/history")
	@Operation(summary = "Get order history", description = "Append-only status/history events for the order.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "History returned"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public List<OrderHistoryResponse> history(@PathVariable UUID id) {
		return orderQueryUseCase.getHistory(id).stream()
				.map(orderHttpMapper::toResponse)
				.toList();
	}
}
