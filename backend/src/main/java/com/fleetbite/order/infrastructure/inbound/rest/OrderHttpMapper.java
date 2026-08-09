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
import org.mapstruct.Mapping;

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

	@Mapping(target = "id", expression = "java(result.id())")
	@Mapping(target = "code", expression = "java(result.code())")
	@Mapping(target = "customerName", expression = "java(result.customerName())")
	@Mapping(target = "customerPhone", expression = "java(result.customerPhone())")
	@Mapping(target = "deliveryAddress", expression = "java(result.deliveryAddress())")
	@Mapping(target = "deliveryLatitude", expression = "java(result.deliveryLatitude())")
	@Mapping(target = "deliveryLongitude", expression = "java(result.deliveryLongitude())")
	@Mapping(target = "totalAmount", expression = "java(result.totalAmount())")
	@Mapping(target = "priority", expression = "java(result.priority().name())")
	@Mapping(target = "status", expression = "java(result.status().name())")
	@Mapping(target = "promisedDeliveryAt", expression = "java(result.promisedDeliveryAt())")
	@Mapping(target = "createdAt", expression = "java(result.createdAt())")
	@Mapping(target = "confirmedAt", expression = "java(result.confirmedAt())")
	@Mapping(target = "preparationStartedAt", expression = "java(result.preparationStartedAt())")
	@Mapping(target = "readyAt", expression = "java(result.readyAt())")
	@Mapping(target = "assignedAt", expression = "java(result.assignedAt())")
	@Mapping(target = "pickedUpAt", expression = "java(result.pickedUpAt())")
	@Mapping(target = "inTransitAt", expression = "java(result.inTransitAt())")
	@Mapping(target = "deliveredAt", expression = "java(result.deliveredAt())")
	@Mapping(target = "cancelledAt", expression = "java(result.cancelledAt())")
	@Mapping(target = "failedDeliveryAt", expression = "java(result.failedDeliveryAt())")
	OrderResponse toResponse(OrderResult result);

	@Mapping(target = "id", expression = "java(result.id())")
	@Mapping(target = "eventType", expression = "java(result.eventType().name())")
	@Mapping(
			target = "previousStatus",
			expression = "java(result.previousStatus() == null ? null : result.previousStatus().name())")
	@Mapping(target = "newStatus", expression = "java(result.newStatus().name())")
	@Mapping(target = "description", expression = "java(result.description())")
	@Mapping(target = "createdAt", expression = "java(result.createdAt())")
	OrderHistoryResponse toResponse(OrderHistoryResult result);
}
