package com.fleetbite.identity.infrastructure.transaction;

import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.in.LoginUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalLoginUseCase implements LoginUseCase {

	private final LoginUseCase delegate;

	public TransactionalLoginUseCase(LoginUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public LoginResult execute(LoginCommand command) {
		return delegate.execute(command);
	}
}
