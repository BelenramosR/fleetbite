package com.fleetbite.driver.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDriverRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 32) String phone,
		@DecimalMin("-90.0") @DecimalMax("90.0") Double currentLatitude,
		@DecimalMin("-180.0") @DecimalMax("180.0") Double currentLongitude) {
}
