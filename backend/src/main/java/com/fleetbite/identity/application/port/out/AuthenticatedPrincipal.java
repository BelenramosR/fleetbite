package com.fleetbite.identity.application.port.out;

import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;

public record AuthenticatedPrincipal(UserId userId, String email, UserRole role) {
}
