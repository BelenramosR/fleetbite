package com.fleetbite.identity.infrastructure.outbound.security;

import com.fleetbite.identity.application.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

	private final PasswordEncoder passwordEncoder;

	public BcryptPasswordEncoderAdapter() {
		this.passwordEncoder = new BCryptPasswordEncoder();
	}

	@Override
	public String hash(String rawPassword) {
		Objects.requireNonNull(rawPassword, "rawPassword is required");
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		Objects.requireNonNull(rawPassword, "rawPassword is required");
		Objects.requireNonNull(passwordHash, "passwordHash is required");
		return passwordEncoder.matches(rawPassword, passwordHash);
	}
}
