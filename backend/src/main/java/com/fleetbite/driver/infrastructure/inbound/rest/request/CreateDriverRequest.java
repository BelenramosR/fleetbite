package com.fleetbite.driver.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Create driver linked to an existing DRIVER user. Optional initial location can be provided.")
public record CreateDriverRequest(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull UUID userId,
		@Schema(example = "988000111", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 32) String phone,
		@Schema(example = "-12.102")
		@DecimalMin("-90.0") @DecimalMax("90.0") Double currentLatitude,
		@Schema(example = "-77.028")
		@DecimalMin("-180.0") @DecimalMax("180.0") Double currentLongitude) {
}
