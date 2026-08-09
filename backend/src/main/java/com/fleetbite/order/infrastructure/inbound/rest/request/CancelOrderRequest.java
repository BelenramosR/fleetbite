package com.fleetbite.order.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional cancel payload. Reason is stored in order history description only.")
public record CancelOrderRequest(
		@Schema(example = "Customer cancelled")
		@Size(max = 500) String reason) {
}
