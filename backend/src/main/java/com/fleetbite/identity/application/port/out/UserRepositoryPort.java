package com.fleetbite.identity.application.port.out;

import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

	User save(User user);

	User update(User user);

	Optional<User> findById(UserId id);

	Optional<User> findByEmail(String email);

	List<User> findAll();

	boolean existsByEmail(String email);
}
