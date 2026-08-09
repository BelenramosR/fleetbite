package com.fleetbite.identity.infrastructure.inbound.rest.response;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
