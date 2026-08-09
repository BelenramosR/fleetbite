package com.fleetbite.order.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Create order. promisedDeliveryAt is calculated by the backend and must not be sent.")
public record CreateOrderRequest(
		@Schema(example = "Ana Torres", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String customerName,
		@Schema(example = "999999999", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String customerPhone,
		@Schema(example = "Av. Example 123", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String deliveryAddress,
		@Schema(example = "-12.1001", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double deliveryLatitude,
		@Schema(example = "-77.0201", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double deliveryLongitude,
		@Schema(example = "85.90", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("0.0") BigDecimal totalAmount) {
}
