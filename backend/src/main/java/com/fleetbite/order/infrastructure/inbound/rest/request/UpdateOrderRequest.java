package com.fleetbite.order.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Update order fields. Allowed only while status is CREATED.")
public record UpdateOrderRequest(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String customerName,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String customerPhone,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank String deliveryAddress,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double deliveryLatitude,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double deliveryLongitude,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("0.0") BigDecimal totalAmount) {
}
