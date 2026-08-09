package com.fleetbite.identity.infrastructure.outbound.persistence;

import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

	private final SpringDataUserRepository springDataUserRepository;
	private final UserPersistenceMapper userPersistenceMapper;

	public UserRepositoryAdapter(
			SpringDataUserRepository springDataUserRepository,
			UserPersistenceMapper userPersistenceMapper) {
		this.springDataUserRepository = Objects.requireNonNull(springDataUserRepository);
		this.userPersistenceMapper = Objects.requireNonNull(userPersistenceMapper);
	}

	@Override
	public User save(User user) {
		Objects.requireNonNull(user, "user is required");
		UserJpaEntity entity = userPersistenceMapper.toEntity(user);
		UserJpaEntity saved = springDataUserRepository.save(entity);
		return userPersistenceMapper.toDomain(saved);
	}

	@Override
	public User update(User user) {
		Objects.requireNonNull(user, "user is required");
		UserJpaEntity existing = springDataUserRepository.findById(user.id().value())
				.orElseThrow(() -> new ResourceNotFoundException("User", user.id().value()));
		userPersistenceMapper.copyToEntity(user, existing);
		UserJpaEntity saved = springDataUserRepository.save(existing);
		return userPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<User> findById(UserId id) {
		Objects.requireNonNull(id, "id is required");
		return springDataUserRepository.findById(id.value())
				.map(userPersistenceMapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		Objects.requireNonNull(email, "email is required");
		return springDataUserRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
				.map(userPersistenceMapper::toDomain);
	}

	@Override
	public List<User> findAll() {
		return springDataUserRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt")).stream()
				.map(userPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public boolean existsByEmail(String email) {
		Objects.requireNonNull(email, "email is required");
		return springDataUserRepository.existsByEmail(email.trim().toLowerCase(Locale.ROOT));
	}
}
