package com.fleetbite.driver.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDriverRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 32) String phone) {
}
