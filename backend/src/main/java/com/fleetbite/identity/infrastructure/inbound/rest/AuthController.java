package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.in.LoginUseCase;
import com.fleetbite.identity.infrastructure.inbound.rest.request.LoginRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final LoginUseCase loginUseCase;
	private final IdentityHttpMapper identityHttpMapper;

	public AuthController(LoginUseCase loginUseCase, IdentityHttpMapper identityHttpMapper) {
		this.loginUseCase = Objects.requireNonNull(loginUseCase);
		this.identityHttpMapper = Objects.requireNonNull(identityHttpMapper);
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = loginUseCase.execute(identityHttpMapper.toCommand(request));
		return identityHttpMapper.toResponse(result);
	}
}
