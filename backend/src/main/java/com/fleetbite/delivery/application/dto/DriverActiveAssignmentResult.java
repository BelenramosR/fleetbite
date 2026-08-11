package com.fleetbite.delivery.application.dto;

import com.fleetbite.order.application.dto.OrderResult;

public record DriverActiveAssignmentResult(AssignmentResult assignment, OrderResult order) {
}
