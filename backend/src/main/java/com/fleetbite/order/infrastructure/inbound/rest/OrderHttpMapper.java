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
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderHttpMapper {

	CreateOrderCommand toCommand(CreateOrderRequest request);

	UpdateOrderCommand toCommand(UpdateOrderRequest request);

	default CancelOrderCommand toCommand(CancelOrderRequest request) {
		if (request == null) {
			return new CancelOrderCommand(null);
		}
		return new CancelOrderCommand(request.reason());
	}

	OrderResponse toResponse(OrderResult result);

	OrderHistoryResponse toResponse(OrderHistoryResult result);
}
