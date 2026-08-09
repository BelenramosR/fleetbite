package com.fleetbite.identity.application.dto;

import com.fleetbite.identity.domain.model.UserRole;

public record UpdateUserCommand(String fullName, UserRole role) {
}
