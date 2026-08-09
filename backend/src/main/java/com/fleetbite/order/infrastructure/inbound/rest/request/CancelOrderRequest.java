package com.fleetbite.order.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.Size;

public record CancelOrderRequest(@Size(max = 500) String reason) {
}
