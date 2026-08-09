package com.fleetbite.identity.application.dto;

public record LoginResult(String accessToken, String tokenType, long expiresIn, String refreshToken) {

	public static LoginResult bearer(String accessToken, long expiresIn, String refreshToken) {
		return new LoginResult(accessToken, "Bearer", expiresIn, refreshToken);
	}
}
