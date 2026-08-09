package com.fleetbite.delivery.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectAssignmentRequest(
		@NotBlank @Size(max = 255) String reason) {
}
