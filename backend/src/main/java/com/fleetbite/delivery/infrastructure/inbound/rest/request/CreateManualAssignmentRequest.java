package com.fleetbite.delivery.infrastructure.inbound.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Manual assignment request")
public record CreateManualAssignmentRequest(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull UUID driverId) {
}
