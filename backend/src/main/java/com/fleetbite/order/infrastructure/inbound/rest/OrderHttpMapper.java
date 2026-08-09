package com.fleetbite.order.infrastructure.inbound.rest;

import com.fleetbite.order.application.dto.CancelOrderCommand;
import com.fleetbite.order.application.dto.CreateOrderCommand;
import com.fleetbite.order.application.dto.OrderHistoryResult;
import com.fleetbite.order.application.dto.OrderResult;
import com.fleetbite.order.application.dto.UpdateOrderCommand;
import com.fleetbite.order.infrastructure.inbound.rest.request.CancelOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.request.CreateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.request.UpdateOrderRequest;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderHistoryResponse;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderHttpMapper {

	public CreateOrderCommand toCommand(CreateOrderRequest request) {
		return new CreateOrderCommand(
				request.customerName(),
				request.customerPhone(),
				request.deliveryAddress(),
				request.deliveryLatitude(),
				request.deliveryLongitude(),
				request.totalAmount());
	}

	public UpdateOrderCommand toCommand(UpdateOrderRequest request) {
		return new UpdateOrderCommand(
				request.customerName(),
				request.customerPhone(),
				request.deliveryAddress(),
				request.deliveryLatitude(),
				request.deliveryLongitude(),
				request.totalAmount());
	}

	public CancelOrderCommand toCommand(CancelOrderRequest request) {
		if (request == null) {
			return new CancelOrderCommand(null);
		}
		return new CancelOrderCommand(request.reason());
	}

	public OrderResponse toResponse(OrderResult result) {
		return new OrderResponse(
				result.id(),
				result.code(),
				result.customerName(),
				result.customerPhone(),
				result.deliveryAddress(),
				result.deliveryLatitude(),
				result.deliveryLongitude(),
				result.totalAmount(),
				result.priority().name(),
				result.status().name(),
				result.promisedDeliveryAt(),
				result.createdAt(),
				result.confirmedAt(),
				result.preparationStartedAt(),
				result.readyAt(),
				result.assignedAt(),
				result.pickedUpAt(),
				result.inTransitAt(),
				result.deliveredAt(),
				result.cancelledAt(),
				result.failedDeliveryAt());
	}

	public OrderHistoryResponse toResponse(OrderHistoryResult result) {
		return new OrderHistoryResponse(
				result.id(),
				result.eventType().name(),
				result.previousStatus() == null ? null : result.previousStatus().name(),
				result.newStatus().name(),
				result.description(),
				result.createdAt());
	}
}
