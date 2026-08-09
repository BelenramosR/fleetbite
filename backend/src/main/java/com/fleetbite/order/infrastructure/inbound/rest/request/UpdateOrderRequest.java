package com.fleetbite.order.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateOrderRequest(
		@NotBlank String customerName,
		@NotBlank String customerPhone,
		@NotBlank String deliveryAddress,
		@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double deliveryLatitude,
		@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double deliveryLongitude,
		@NotNull @DecimalMin("0.0") BigDecimal totalAmount) {
}
