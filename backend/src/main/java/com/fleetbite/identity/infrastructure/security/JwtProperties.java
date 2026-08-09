package com.fleetbite.identity.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fleetbite.security.jwt")
public class JwtProperties {

	private String secret;
	private long expirationSeconds = 3600;
	private long refreshExpirationSeconds = 604800;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	public void setExpirationSeconds(long expirationSeconds) {
		this.expirationSeconds = expirationSeconds;
	}

	public long getRefreshExpirationSeconds() {
		return refreshExpirationSeconds;
	}

	public void setRefreshExpirationSeconds(long refreshExpirationSeconds) {
		this.refreshExpirationSeconds = refreshExpirationSeconds;
	}
}
