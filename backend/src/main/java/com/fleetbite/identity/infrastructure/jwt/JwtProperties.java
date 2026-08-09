package com.fleetbite.identity.infrastructure.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades JWT (paquete sin estereotipo hexagonal = Unannotated).
 * Compartido por filtro (primary) y TokenProvider (secondary) sin cruzar adapters.
 */
@ConfigurationProperties(prefix = "fleetbite.security.jwt")
@Getter
@Setter
public class JwtProperties {

	private String secret;
	private long expirationSeconds = 3600;
	private long refreshExpirationSeconds = 604800;
}
