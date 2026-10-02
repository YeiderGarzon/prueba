package com.prueba.prueba.application.port.out;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.UserRole;

public interface UserStore {
	Optional<AppUser> findById(Long id);

	Optional<AppUser> findByUsername(String username);

	List<AppUser> findAll();

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Long id);

	long countByRole(UserRole role);

	AppUser save(AppUser user);

	void delete(AppUser user);
}
