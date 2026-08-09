package com.fleetbite.identity.application.port.out;

import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;

public interface TokenProviderPort {

	String generate(UserId userId, String email, UserRole role);

	AuthenticatedPrincipal parse(String token);

	long expiresInSeconds();
}
