package com.fleetbite.delivery.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Reject assignment with required reason")
public record RejectAssignmentRequest(
		@Schema(example = "Vehicle problem", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank @Size(max = 255) String reason) {
}
