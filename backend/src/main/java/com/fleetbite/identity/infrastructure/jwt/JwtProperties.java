package com.fleetbite.identity.infrastructure.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades JWT (paquete sin estereotipo hexagonal = Unannotated).
 * Compartido por filtro (primary) y TokenProvider (secondary) sin cruzar adapters.
 */
@ConfigurationProperties(prefix = "fleetbite.security.jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

	@NotBlank
	@Size(min = 32)
	private String secret;
	private long expirationSeconds = 3600;
	private long refreshExpirationSeconds = 604800;
}
