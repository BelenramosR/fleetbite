package com.fleetbite.identity.application.dto;

import com.fleetbite.identity.domain.model.UserRole;

public record CreateUserCommand(String email, String password, String fullName, UserRole role) {
}
