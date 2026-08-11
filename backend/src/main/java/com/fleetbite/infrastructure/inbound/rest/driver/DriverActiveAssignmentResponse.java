package com.fleetbite.infrastructure.inbound.rest.driver;

import com.fleetbite.delivery.infrastructure.inbound.rest.response.AssignmentResponse;
import com.fleetbite.order.infrastructure.inbound.rest.response.OrderResponse;

public record DriverActiveAssignmentResponse(AssignmentResponse assignment, OrderResponse order) {
}
