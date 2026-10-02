package com.prueba.prueba.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaUserStore implements UserStore {
	private final SpringDataUserRepository repository;

	JpaUserStore(SpringDataUserRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AppUser> findById(Long id) {
		return repository.findById(id).map(UserEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AppUser> findByUsername(String username) {
		return repository.findByUsernameIgnoreCase(username).map(UserEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AppUser> findAll() {
		return repository.findAll().stream().map(UserEntity::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return repository.existsByUsernameIgnoreCase(username);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsernameAndIdNot(String username, Long id) {
		return repository.existsByUsernameIgnoreCaseAndIdNot(username, id);
	}

	@Override
	@Transactional(readOnly = true)
	public long countByRole(UserRole role) {
		return repository.countByRole(role);
	}

	@Override
	@Transactional
	public AppUser save(AppUser user) {
		return repository.save(new UserEntity(user)).toDomain();
	}

	@Override
	@Transactional
	public void delete(AppUser user) {
		repository.deleteById(user.getId());
	}
}
