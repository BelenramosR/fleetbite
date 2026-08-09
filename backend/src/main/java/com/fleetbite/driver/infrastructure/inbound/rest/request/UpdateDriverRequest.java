package com.fleetbite.driver.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Update driver phone")
public record UpdateDriverRequest(
		@Schema(example = "988000111", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 32) String phone) {
}
