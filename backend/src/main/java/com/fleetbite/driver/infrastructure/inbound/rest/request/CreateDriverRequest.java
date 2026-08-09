package com.fleetbite.driver.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Create driver. Optional initial location can be provided.")
public record CreateDriverRequest(
		@Schema(example = "Luis Gómez", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 120) String name,
		@Schema(example = "988000111", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 32) String phone,
		@Schema(example = "-12.102")
		@DecimalMin("-90.0") @DecimalMax("90.0") Double currentLatitude,
		@Schema(example = "-77.028")
		@DecimalMin("-180.0") @DecimalMax("180.0") Double currentLongitude) {
}
