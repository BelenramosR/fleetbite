package com.fleetbite.driver.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update current driver coordinates")
public record UpdateDriverLocationRequest(
		@Schema(example = "-12.102", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
		@Schema(example = "-77.028", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
}
