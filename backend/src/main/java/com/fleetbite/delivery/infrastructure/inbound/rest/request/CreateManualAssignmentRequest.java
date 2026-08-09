package com.fleetbite.delivery.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateManualAssignmentRequest(@NotNull UUID driverId) {
}
