package com.fleetbite.identity.application.port.out;

import java.util.UUID;

import com.fleetbite.identity.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

	User save(User user);

	User update(User user);

	Optional<User> findById(UUID id);

	Optional<User> findByEmail(String email);

	List<User> findAll();

	boolean existsByEmail(String email);
}
