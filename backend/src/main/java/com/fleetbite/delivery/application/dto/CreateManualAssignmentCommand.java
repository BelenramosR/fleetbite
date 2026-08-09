package com.fleetbite.delivery.application.dto;

import java.util.UUID;

public record CreateManualAssignmentCommand(UUID orderId, UUID driverId) {
}
