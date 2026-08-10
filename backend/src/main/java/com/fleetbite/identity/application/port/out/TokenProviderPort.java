package com.fleetbite.identity.application.port.out;

import java.util.UUID;

import com.fleetbite.identity.application.dto.AuthenticatedPrincipal;
import com.fleetbite.identity.domain.model.UserRole;

public interface TokenProviderPort {

	String generate(UUID userId, String email, UserRole role);

	AuthenticatedPrincipal parse(String token);

	long expiresInSeconds();
}
