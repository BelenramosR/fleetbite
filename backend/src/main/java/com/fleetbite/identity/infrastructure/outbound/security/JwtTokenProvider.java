package com.fleetbite.identity.infrastructure.outbound.security;

import com.fleetbite.identity.application.dto.AuthenticatedPrincipal;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.identity.infrastructure.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProviderPort {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = Objects.requireNonNull(jwtProperties);
		String secret = jwtProperties.getSecret();
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("fleetbite.security.jwt.secret (JWT_SECRET) must be configured");
		}
		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
		}
		this.secretKey = Keys.hmacShaKeyFor(secretBytes);
	}

	@Override
	public String generate(UUID userId, String email, UserRole role) {
		Objects.requireNonNull(userId, "userId is required");
		Objects.requireNonNull(email, "email is required");
		Objects.requireNonNull(role, "role is required");

		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(jwtProperties.getExpirationSeconds());

		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.claim("role", role.name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(secretKey)
				.compact();
	}

	@Override
	public AuthenticatedPrincipal parse(String token) {
		Objects.requireNonNull(token, "token is required");
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			UUID userId = UUID.fromString(claims.getSubject());
			String email = claims.get("email", String.class);
			String roleName = claims.get("role", String.class);
			if (email == null || roleName == null) {
				throw new AuthenticationFailedException();
			}
			return new AuthenticatedPrincipal(userId, email, UserRole.valueOf(roleName));
		}
		catch (IllegalArgumentException | JwtException exception) {
			throw new AuthenticationFailedException();
		}
	}

	@Override
	public long expiresInSeconds() {
		return jwtProperties.getExpirationSeconds();
	}
}
