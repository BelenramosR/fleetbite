package com.fleetbite.identity.application.dto;

import java.util.UUID;

import com.fleetbite.identity.domain.model.UserRole;

public record AuthenticatedPrincipal(UUID userId, String email, UserRole role) {
}
