package com.prueba.prueba.infrastructure.security;

import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.domain.model.AppUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JpaUserDetailsService implements UserDetailsService {
	private final UserStore users;

	public JpaUserDetailsService(UserStore users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser user = users.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
		return User.withUsername(user.getUsername())
				.password(user.getPasswordHash())
				.roles(user.getRole().name())
				.build();
	}
}
