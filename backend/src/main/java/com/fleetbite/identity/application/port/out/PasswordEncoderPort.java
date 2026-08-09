package com.fleetbite.identity.application.port.out;

public interface PasswordEncoderPort {

	String hash(String rawPassword);

	boolean matches(String rawPassword, String passwordHash);
}
