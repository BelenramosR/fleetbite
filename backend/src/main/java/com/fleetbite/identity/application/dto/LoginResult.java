package com.fleetbite.identity.application.dto;

public record LoginResult(String accessToken, String tokenType, long expiresIn) {

	public static LoginResult bearer(String accessToken, long expiresIn) {
		return new LoginResult(accessToken, "Bearer", expiresIn);
	}
}
