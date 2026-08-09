package com.fleetbite.identity.infrastructure.transaction;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;
import com.fleetbite.identity.application.port.in.RefreshAccessTokenUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalRefreshAccessTokenUseCase implements RefreshAccessTokenUseCase {

	private final RefreshAccessTokenUseCase delegate;

	public TransactionalRefreshAccessTokenUseCase(RefreshAccessTokenUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public LoginResult execute(RefreshTokenCommand command) {
		return delegate.execute(command);
	}
}
