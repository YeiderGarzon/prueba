package com.prueba.prueba.infrastructure.persistence;

import java.util.Optional;

import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

	long countByRole(UserRole role);
}
